package org.carlspring.cloud.storage.s3fs;

import org.carlspring.cloud.storage.s3fs.util.AmazonS3ClientMock;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.ACCESS_KEY;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.SECRET_KEY;
import static org.carlspring.cloud.storage.s3fs.S3FileSystemProvider.AMAZON_S3_FACTORY_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class S3UnitTestBase
{

    private S3FileSystemProvider s3fsProvider;


    @BeforeClass
    public static void setProperties()
    {
        System.clearProperty(S3FileSystemProvider.AMAZON_S3_FACTORY_CLASS);
        System.clearProperty(ACCESS_KEY);
        System.clearProperty(SECRET_KEY);

        System.setProperty(AMAZON_S3_FACTORY_CLASS, "org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory");
    }

    @Before
    public void setupS3fsProvider()
    {
        s3fsProvider = spy(new S3FileSystemProvider());

        // stub the possibility to add system envs var
        doReturn(false).when(s3fsProvider).overloadPropertiesWithSystemEnv(any(Properties.class), anyString());
        doReturn(new Properties()).when(s3fsProvider).loadAmazonProperties();
    }

    @After
    public void closeMemory()
    {
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.clear();

        for (S3FileSystem s3FileSystem : S3FileSystemProvider.getFilesystems().values())
        {
            try
            {
                s3FileSystem.close();
            }
            catch (Exception e)
            {
                //ignore
            }
        }
    }

    public S3FileSystemProvider getS3fsProvider()
    {
        return this.s3fsProvider;
    }

}
