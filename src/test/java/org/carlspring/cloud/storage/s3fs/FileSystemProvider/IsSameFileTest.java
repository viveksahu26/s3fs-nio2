package org.carlspring.cloud.storage.s3fs.FileSystemProvider;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;
import org.carlspring.cloud.storage.s3fs.S3FileSystemProvider;
import org.carlspring.cloud.storage.s3fs.S3UnitTestBase;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3ClientMock;
import org.carlspring.cloud.storage.s3fs.util.AmazonS3MockFactory;
import org.carlspring.cloud.storage.s3fs.util.S3EndpointConstant;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class IsSameFileTest
        extends S3UnitTestBase
{

    private S3FileSystemProvider s3fsProvider;


    @Before
    public void setup()
            throws IOException
    {
        s3fsProvider = getS3fsProvider();
        s3fsProvider.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
    }

    @Test
    public void isSameFileTrue()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir").file("dir/file1");

        // act
        FileSystem fs = createNewS3FileSystem();
        Path file1 = fs.getPath("/bucketA/dir/file1");
        Path fileCopy = fs.getPath("/bucketA/dir/file1");

        // assert
        assertTrue(s3fsProvider.isSameFile(file1, fileCopy));
    }

    @Test
    public void isSameFileFalse()
            throws IOException
    {
        // fixtures
        AmazonS3ClientMock client = AmazonS3MockFactory.getAmazonClientMock();
        client.bucket("bucketA").dir("dir", "dir2").file("dir/file1", "dir2/file13");

        // act
        FileSystem fs = createNewS3FileSystem();
        Path file1 = fs.getPath("/bucketA/dir/file1");
        Path fileCopy = fs.getPath("/bucketA/dir2/file2");

        // assert
        assertTrue(!s3fsProvider.isSameFile(file1, fileCopy));
    }

    /**
     * create a new file system for s3 scheme with fake credentials
     * and global endpoint
     *
     * @return FileSystem
     * @throws IOException
     */
    private S3FileSystem createNewS3FileSystem()
            throws IOException
    {
        try
        {
            return s3fsProvider.getFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST);
        }
        catch (FileSystemNotFoundException e)
        {
            return (S3FileSystem) FileSystems.newFileSystem(S3EndpointConstant.S3_GLOBAL_URI_TEST, null);
        }
    }

}
