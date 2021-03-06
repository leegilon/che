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
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;

import java.util.Collection;

/**
 * @author Yevhenii Voevodin
 */
public class WorkspaceTckModule extends TckModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("main"));
        bind(DBInitializer.class).asEagerSingleton();
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(H2TestHelper.inMemoryDefault(), "che-schema"));
        bind(TckResourcesCleaner.class).to(H2JpaCleaner.class);

        bind(new TypeLiteral<TckRepository<AccountImpl>>() {}).toInstance(new JpaTckRepository<>(AccountImpl.class));
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new WorkspaceRepository());
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new StackRepository());

        bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
        bind(StackDao.class).to(JpaStackDao.class);
    }

    private static class WorkspaceRepository extends JpaTckRepository<WorkspaceImpl> {
        public WorkspaceRepository() { super(WorkspaceImpl.class); }

        @Override
        public void createAll(Collection<? extends WorkspaceImpl> entities) throws TckRepositoryException {
            for (WorkspaceImpl entity : entities) {
                entity.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
            }
            super.createAll(entities);
        }
    }

    private static class StackRepository extends JpaTckRepository<StackImpl> {
        public StackRepository() { super(StackImpl.class); }

        @Override
        public void createAll(Collection<? extends StackImpl> entities) throws TckRepositoryException {
            for (StackImpl stack : entities) {
                stack.getWorkspaceConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
            }
            super.createAll(entities);
        }
    }
}
