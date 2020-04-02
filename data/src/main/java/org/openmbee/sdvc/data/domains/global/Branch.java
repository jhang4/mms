package org.openmbee.sdvc.data.domains.global;

import java.util.Collection;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "branches",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"branchid","project_id"})
    })
public class Branch extends Base {

    @ManyToOne
    private Project project;

    private String branchId;

    private boolean inherit;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Collection<BranchGroupPerm> groupPerms;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Collection<BranchUserPerm> userPerms;

    public Branch() {}

    public Branch(Project p, String branchId, boolean inherit) {
        this.project = p;
        this.branchId = branchId;
        this.inherit = inherit;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Collection<BranchUserPerm> getUserPerms() {
        return userPerms;
    }

    public void setUserPerms(Collection<BranchUserPerm> userPerms) {
        this.userPerms = userPerms;
    }

    public Collection<BranchGroupPerm> getGroupPerms() {
        return groupPerms;
    }

    public void setGroupPerms(Collection<BranchGroupPerm> groupPerms) {
        this.groupPerms = groupPerms;
    }

    public boolean isInherit() {
        return inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return
            Objects.equals(getProject(), branch.getProject()) &&
            Objects.equals(getBranchId(), branch.getBranchId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProject(), getBranchId());
    }
}
