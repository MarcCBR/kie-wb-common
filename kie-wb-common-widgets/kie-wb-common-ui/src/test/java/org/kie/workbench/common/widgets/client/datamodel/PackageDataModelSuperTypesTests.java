package org.kie.workbench.common.widgets.client.datamodel;

import java.net.URL;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.drools.workbench.models.datamodel.oracle.PackageDataModelOracle;
import org.drools.workbench.models.datamodel.oracle.ProjectDataModelOracle;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.weld.environment.se.StartMain;
import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.services.datamodel.backend.server.service.DataModelService;
import org.kie.workbench.common.services.datamodel.model.PackageDataModelOracleBaselinePayload;
import org.kie.workbench.common.services.datamodel.service.IncrementalDataModelService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.java.nio.fs.file.SimpleFileSystemProvider;

import static org.junit.Assert.*;
import static org.kie.workbench.common.widgets.client.datamodel.PackageDataModelOracleTestUtils.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DataModelService
 */
public class PackageDataModelSuperTypesTests {

    private final SimpleFileSystemProvider fs = new SimpleFileSystemProvider();
    private BeanManager beanManager;
    private Paths paths;

    @Before
    public void setUp() throws Exception {
        //Bootstrap WELD container
        StartMain startMain = new StartMain( new String[ 0 ] );
        beanManager = startMain.go().getBeanManager();

        //Instantiate Paths used in tests for Path conversion
        final Bean pathsBean = (Bean) beanManager.getBeans( Paths.class ).iterator().next();
        final CreationalContext cc = beanManager.createCreationalContext( pathsBean );
        paths = (Paths) beanManager.getReference( pathsBean,
                                                  Paths.class,
                                                  cc );

        //Ensure URLs use the default:// scheme
        fs.forceAsDefault();
    }

    @Test
    public void testPackageSuperTypes() throws Exception {
        final Bean dataModelServiceBean = (Bean) beanManager.getBeans( DataModelService.class ).iterator().next();
        final CreationalContext cc = beanManager.createCreationalContext( dataModelServiceBean );
        final DataModelService dataModelService = (DataModelService) beanManager.getReference( dataModelServiceBean,
                                                                                               DataModelService.class,
                                                                                               cc );

        final URL packageUrl = this.getClass().getResource( "/DataModelBackendSuperTypesTest1/src/main/java/t2p1" );
        final org.uberfire.java.nio.file.Path nioPackagePath = fs.getPath( packageUrl.toURI() );
        final Path packagePath = paths.convert( nioPackagePath );

        final PackageDataModelOracle packageLoader = dataModelService.getDataModel( packagePath );

        //Emulate server-to-client conversions
        final MockAsyncPackageDataModelOracleImpl oracle = new MockAsyncPackageDataModelOracleImpl();
        final Caller<IncrementalDataModelService> service = new MockIncrementalDataModelServiceCaller();
        oracle.setService( service );

        final PackageDataModelOracleBaselinePayload dataModel = new PackageDataModelOracleBaselinePayload();
        dataModel.setModelFields( packageLoader.getProjectModelFields() );
        PackageDataModelOracleTestUtils.populateDataModelOracle( mock( Path.class ),
                                                                 new MockHasImports(),
                                                                 oracle,
                                                                 dataModel );

        assertNotNull( oracle );

        assertEquals( 3,
                      oracle.getFactTypes().length );
        assertContains( "Bean1",
                        oracle.getFactTypes() );
        assertContains( "Bean2",
                        oracle.getFactTypes() );
        assertContains( "Bean4",
                        oracle.getFactTypes() );

        oracle.getSuperType( "Bean1",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertNull( result );
                                 }
                             } );
        oracle.getSuperType( "Bean2",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertEquals( "Bean1",
                                                   result );
                                 }
                             } );
        oracle.getSuperType( "Bean4",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertEquals( "t2p2.Bean3",
                                                   result );
                                 }
                             } );
    }

    @Test
    public void testProjectSuperTypes() throws Exception {
        final Bean dataModelServiceBean = (Bean) beanManager.getBeans( DataModelService.class ).iterator().next();
        final CreationalContext cc = beanManager.createCreationalContext( dataModelServiceBean );
        final DataModelService dataModelService = (DataModelService) beanManager.getReference( dataModelServiceBean,
                                                                                               DataModelService.class,
                                                                                               cc );

        final URL packageUrl = this.getClass().getResource( "/DataModelBackendSuperTypesTest1/src/main/java/t2p1" );
        final org.uberfire.java.nio.file.Path nioPackagePath = fs.getPath( packageUrl.toURI() );
        final Path packagePath = paths.convert( nioPackagePath );

        final ProjectDataModelOracle packageLoader = dataModelService.getProjectDataModel( packagePath );

        //Emulate server-to-client conversions
        final MockAsyncPackageDataModelOracleImpl oracle = new MockAsyncPackageDataModelOracleImpl();
        final Caller<IncrementalDataModelService> service = new MockIncrementalDataModelServiceCaller();
        oracle.setService( service );

        final PackageDataModelOracleBaselinePayload dataModel = new PackageDataModelOracleBaselinePayload();
        dataModel.setModelFields( packageLoader.getProjectModelFields() );
        PackageDataModelOracleTestUtils.populateDataModelOracle( mock( Path.class ),
                                                                 new MockHasImports(),
                                                                 oracle,
                                                                 dataModel );

        assertNotNull( oracle );

        assertEquals( 4,
                      oracle.getFactTypes().length );
        assertContains( "t2p1.Bean1",
                        oracle.getFactTypes() );
        assertContains( "t2p1.Bean2",
                        oracle.getFactTypes() );
        assertContains( "t2p2.Bean3",
                        oracle.getFactTypes() );
        assertContains( "t2p1.Bean4",
                        oracle.getFactTypes() );

        oracle.getSuperType( "t2p1.Bean1",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertNull( result );
                                 }
                             } );
        oracle.getSuperType( "t2p1.Bean2",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertEquals( "t2p1.Bean1",
                                                   result );
                                 }
                             } );
        oracle.getSuperType( "t2p2.Bean3",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertEquals( "t2p1.Bean1",
                                                   result );
                                 }
                             } );
        oracle.getSuperType( "t2p1.Bean4",
                             new Callback<String>() {
                                 @Override
                                 public void callback( final String result ) {
                                     assertEquals( "t2p2.Bean3",
                                                   result );
                                 }
                             } );
    }

}
