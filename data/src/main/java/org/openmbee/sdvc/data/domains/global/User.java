package org.openmbee.sdvc.data.domains.global;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Objects;
import javax.persistence.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")})
public class User extends Base {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean admin;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean enabled;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<ProjectUserPerm> projectPerms;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<OrgUserPerm> orgPerms;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Collection<BranchUserPerm> branchPerms;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_groups", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    private Collection<Group> groups;

    public User() {
    }

    public User(String email, String username, String password, String firstName, String lastName, boolean admin) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void encodePassword(String password) {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        this.password = bcrypt.encode(password);
    }

    public void updatePassword(String old, String newPass1, String newPass2) {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        if (!password.equals(bcrypt.encode(old))) {
            throw new IllegalArgumentException("Existing Password invalid");
        }
        if (!newPass1.equals(newPass2)) {
            throw new IllegalArgumentException("New Passwords don't match");
        }
        this.password = bcrypt.encode(newPass1);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Collection<ProjectUserPerm> getProjectPerms() {
        return projectPerms;
    }

    public void setProjectPerms(Collection<ProjectUserPerm> projectPerms) {
        this.projectPerms = projectPerms;
    }

    public Collection<OrgUserPerm> getOrgPerms() {
        return orgPerms;
    }

    public void setOrgPerms(Collection<OrgUserPerm> orgPerms) {
        this.orgPerms = orgPerms;
    }

    public Collection<BranchUserPerm> getBranchPerms() {
        return branchPerms;
    }

    public void setBranchPerms(Collection<BranchUserPerm> branchPerms) {
        this.branchPerms = branchPerms;
    }

    public Collection<Group> getGroups() {
        return groups;
    }

    public void setGroups(Collection<Group> groups) {
        this.groups = groups;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isAdmin() == user.isAdmin() &&
            isEnabled() == user.isEnabled() &&
            Objects.equals(getUsername(), user.getUsername()) &&
            Objects.equals(getEmail(), user.getEmail()) &&
            Objects.equals(getFirstName(), user.getFirstName()) &&
            Objects.equals(getLastName(), user.getLastName()) &&
            Objects.equals(getPassword(), user.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getEmail(), getFirstName(), getLastName(), isAdmin(), getPassword(), isEnabled());
    }
}
