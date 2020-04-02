package org.openmbee.sdvc.data.domains.global;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "project_group_perms",
    indexes = {
        @Index(columnList = "project_id"),
        @Index(columnList = "project_id,group_id"),
        @Index(columnList = "project_id,inherited")
    })
public class ProjectGroupPerm extends Base {

    @ManyToOne
    private Project project;

    @ManyToOne
    private Group group;

    @ManyToOne
    private Role role;

    private boolean inherited;

    public ProjectGroupPerm() {}

    public ProjectGroupPerm(Project p, Group u, Role r, boolean inherited) {
        this.project = p;
        this.group = u;
        this.role = r;
        this.inherited = inherited;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectGroupPerm that = (ProjectGroupPerm) o;
        return isInherited() == that.isInherited() &&
            Objects.equals(getProject(), that.getProject()) &&
            Objects.equals(getGroup(), that.getGroup()) &&
            Objects.equals(getRole(), that.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProject(), getGroup(), getRole(), isInherited());
    }
}
