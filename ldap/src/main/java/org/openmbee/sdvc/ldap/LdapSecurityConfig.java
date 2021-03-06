package org.openmbee.sdvc.ldap;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.sdvc.core.config.AuthorizationConstants;
import org.openmbee.sdvc.data.domains.global.Group;
import org.openmbee.sdvc.rdb.repositories.GroupRepository;
import org.openmbee.sdvc.rdb.repositories.UserRepository;
import org.openmbee.sdvc.data.domains.global.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.filter.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class LdapSecurityConfig {

    private static Logger logger = LogManager.getLogger(LdapSecurityConfig.class);

    @Value("${ldap.provider.url}")
    private String providerUrl;

    @Value("${ldap.provider.userdn}")
    private String providerUserDn;

    @Value("${ldap.provider.password}")
    private String providerPassword;

    @Value("${ldap.provider.base}")
    private String providerBase;

    @Value("${ldap.user.dn.pattern}")
    private String userDnPattern;

    @Value("${ldap.user.attributes.username}")
    private String userAttributesUsername;

    @Value("${ldap.user.attributes.email}")
    private String userAttributesEmail;

    @Value("${ldap.group.search.base}")
    private String groupSearchBase;

    @Value("${ldap.group.role.attribute}")
    private String groupRoleAttribute;

    @Value("${ldap.group.search.filter}")
    private String groupSearchFilter;

    private UserRepository userRepository;
    private GroupRepository groupRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void configureLdapAuth(AuthenticationManagerBuilder auth,
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator, @Qualifier("contextSource") BaseLdapPathContextSource contextSource)
        throws Exception {
        logger.error("LDAP IS HAPPENING!!!");
        /*
            see this article : https://spring.io/guides/gs/authenticating-ldap/
            We  redefine our own LdapAuthoritiesPopulator which need ContextSource().
            We need to delegate the creation of the contextSource out of the builder-configuration.
        */
        auth.ldapAuthentication().userDnPatterns(userDnPattern).groupSearchBase(groupSearchBase)
            .groupRoleAttribute(groupRoleAttribute).groupSearchFilter(groupSearchFilter)
            .rolePrefix("")
            .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator)
            .contextSource(contextSource);
    }

    @Bean
    LdapAuthoritiesPopulator ldapAuthoritiesPopulator(@Qualifier("contextSource") BaseLdapPathContextSource baseContextSource) {

        /*
          Specificity here : we don't get the Role by reading the members of available groups (which is implemented by
          default in Spring security LDAP), but we retrieve the groups from the field memberOf of the user.
         */
        class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

            SpringSecurityLdapTemplate ldapTemplate;

            private CustomLdapAuthoritiesPopulator(BaseLdapPathContextSource ldapContextSource) {
                ldapTemplate = new SpringSecurityLdapTemplate(ldapContextSource);
            }

            @Override
            public Collection<? extends GrantedAuthority> getGrantedAuthorities(
                DirContextOperations userData, String username) {
                Optional<User> userOptional = userRepository.findByUsername(username);
                if (!userOptional.isPresent()) {
                    User newUser = new User();
                    newUser.setEmail(userData.getStringAttribute(userAttributesEmail));
                    newUser.setUsername(userData.getStringAttribute(userAttributesUsername));
                    newUser.setEnabled(true);
                    newUser.setAdmin(false);
                    userRepository.save(newUser);

                    userOptional = Optional.of(newUser);
                }

                User user = userOptional.get();
                user.setPassword(null);
                String userDn = "uid=" + user.getUsername() + "," + providerBase;

                List<Group> definedGroups = groupRepository.findAll();
                OrFilter orFilter = new OrFilter();

                for (int i = 0; i < definedGroups.size(); i++) {
                    orFilter.or(new EqualsFilter("cn", definedGroups.get(i).getName()));
                }

                AndFilter andFilter = new AndFilter();
                HardcodedFilter groupsFilter = new HardcodedFilter(
                    groupSearchFilter.replace("{0}", userDn));
                andFilter.and(groupsFilter);
                andFilter.and(orFilter);

                Set<String> memberGroups = ldapTemplate
                    .searchForSingleAttributeValues("", andFilter.encode(), new Object[]{""},
                        groupRoleAttribute);

                Set<Group> addGroups = new HashSet<>();
                for (String memberGroup : memberGroups) {
                    Optional<Group> group = groupRepository.findByName(memberGroup);
                    group.ifPresent(addGroups::add);
                }
                user.setGroups(addGroups);
                userRepository.save(user);

                List<GrantedAuthority> auths = AuthorityUtils
                    .createAuthorityList(memberGroups.toArray(new String[0]));
                if (user.isAdmin()) {
                    auths.add(new SimpleGrantedAuthority(AuthorizationConstants.MMSADMIN));
                }
                auths.add(new SimpleGrantedAuthority(AuthorizationConstants.EVERYONE));
                return auths;
            }
        }

        return new CustomLdapAuthoritiesPopulator(baseContextSource);

    }

    @Bean
    public BaseLdapPathContextSource contextSource() {
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(
            providerUrl);
        contextSource.setUserDn(providerUserDn);
        contextSource.setPassword(providerPassword);
        return contextSource;
    }

}