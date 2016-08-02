/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.factory.server.FactoryImage;
import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.Policies;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Data object for {@link Factory}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Factory")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "userId"})})
public class FactoryImpl implements Factory {

    public static FactoryImplBuilder builder() {
        return new FactoryImplBuilder();
    }

    @Id
    private String id;

    @Basic
    private String name;

    @Column(nullable = false)
    private String version;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private WorkspaceConfigImpl workspace;

    @Embedded
    private AuthorImpl creator;

    @OneToOne
    @JoinColumn(insertable = false, updatable = false, name = "userId")
    private UserImpl userEntity;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ButtonImpl button;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private IdeImpl ide;

    @Embedded
    private PoliciesImpl policies;

    @ElementCollection
    private Set<FactoryImage> images;

    public FactoryImpl() {}

    public FactoryImpl(String id,
                       String name,
                       String version,
                       WorkspaceConfig workspace,
                       AuthorImpl creator,
                       PoliciesImpl policies,
                       Ide ide,
                       ButtonImpl button,
                       Set<FactoryImage> images) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.workspace = new WorkspaceConfigImpl(workspace);
        this.creator = creator;
        this.policies = policies;
        if (ide != null) {
            this.ide = new IdeImpl(ide);
        }
        this.button = button;
        this.images = images;
    }

    public FactoryImpl(Factory factory, Set<FactoryImage> images) {
        this(factory.getId(),
             factory.getName(),
             factory.getV(),
             new WorkspaceConfigImpl(factory.getWorkspace()),
             new AuthorImpl(factory.getCreator()),
             new PoliciesImpl(factory.getPolicies()),
             new IdeImpl(factory.getIde()),
             new ButtonImpl(factory.getButton()),
             images);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getV() {
        return version;
    }

    public void setV(String version) {
        this.version = version;
    }

    @Override
    public WorkspaceConfigImpl getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceConfigImpl workspace) {
        this.workspace = workspace;
    }

    @Override
    public AuthorImpl getCreator() {
        return creator;
    }

    public void setCreator(AuthorImpl creator) {
        this.creator = creator;
    }

    @Override
    public PoliciesImpl getPolicies() {
        return policies;
    }

    public void setPolicies(PoliciesImpl policies) {
        this.policies = policies;
    }

    @Override
    public ButtonImpl getButton() {
        return button;
    }

    public void setButton(ButtonImpl button) {
        this.button = button;
    }

    @Override
    public IdeImpl getIde() {
        return ide;
    }

    public void setIde(IdeImpl ide) {
        this.ide = ide;
    }

    public Set<FactoryImage> getImages() {
        if (images == null) {
            images = new HashSet<>();
        }
        return images;
    }

    public void setImages(Set<FactoryImage> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FactoryImpl)) return false;
        final FactoryImpl other = (FactoryImpl)obj;
        return Objects.equals(id, other.id)
               && Objects.equals(name, other.name)
               && Objects.equals(version, other.version)
               && Objects.equals(workspace, other.workspace)
               && Objects.equals(creator, other.creator)
               && Objects.equals(policies, other.policies)
               && Objects.equals(ide, other.ide)
               && Objects.equals(button, other.button)
               && getImages().equals(other.getImages());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(version);
        hash = 31 * hash + Objects.hashCode(workspace);
        hash = 31 * hash + Objects.hashCode(creator);
        hash = 31 * hash + Objects.hashCode(policies);
        hash = 31 * hash + Objects.hashCode(ide);
        hash = 31 * hash + Objects.hashCode(button);
        hash = 31 * hash + getImages().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "FactoryImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", workspace=" + workspace +
               ", creator=" + creator +
               ", policies=" + policies +
               ", ide=" + ide +
               ", button=" + button +
               ", images=" + images +
               '}';
    }

    /**
     * Helps to create the instance of {@link FactoryImpl}.
     */
    public static class FactoryImplBuilder {

        private String              id;
        private String              name;
        private String              version;
        private WorkspaceConfigImpl workspace;
        private AuthorImpl          creator;
        private PoliciesImpl        policies;
        private IdeImpl             ide;
        private ButtonImpl          button;
        private Set<FactoryImage>   images;

        private FactoryImplBuilder() {}

        public FactoryImpl build() {
            return new FactoryImpl(id, name, version, workspace, creator, policies, ide, button, images);
        }

        public FactoryImplBuilder from(FactoryImpl factory) {
            this.id = factory.getId();
            this.name = factory.getName();
            this.version = factory.getV();
            this.workspace = factory.getWorkspace();
            this.creator = factory.getCreator();
            this.policies = factory.getPolicies();
            this.ide = factory.getIde();
            this.button = factory.getButton();
            this.images = factory.getImages();
            return this;
        }

        public FactoryImplBuilder generateId() {
            id = NameGenerator.generate("", 16);
            return this;
        }

        public FactoryImplBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public FactoryImplBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public FactoryImplBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public FactoryImplBuilder setWorkspace(WorkspaceConfig workspace) {
            this.workspace = new WorkspaceConfigImpl(workspace);
            return this;
        }

        public FactoryImplBuilder setCreator(AuthorImpl creator) {
            this.creator = creator;
            return this;
        }

        public FactoryImplBuilder setPolicies(PoliciesImpl policies) {
            this.policies = policies;
            return this;
        }

        public FactoryImplBuilder setIde(IdeImpl ide) {
            this.ide = ide;
            return this;
        }

        public FactoryImplBuilder setButton(ButtonImpl button) {
            this.button = button;
            return this;
        }

        public FactoryImplBuilder setImages(Set<FactoryImage> images) {
            this.images = images;
            return this;
        }
    }
}
