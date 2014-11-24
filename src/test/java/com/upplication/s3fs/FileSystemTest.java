package com.upplication.s3fs;

import static com.upplication.s3fs.AmazonS3Factory.ACCESS_KEY;
import static com.upplication.s3fs.AmazonS3Factory.SECRET_KEY;
import static com.upplication.s3fs.S3UnitTest.S3_GLOBAL_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.google.common.collect.ImmutableMap;
import com.upplication.s3fs.util.AmazonS3ClientMock;

public class FileSystemTest {
	
	private FileSystem fs;
//	private FileSystem fsMem;
	private S3FileSystemProvider provider;
	
	@Before
	public void setup() throws IOException{
		fs = FileSystems.getFileSystem(S3_GLOBAL_URI);
		AmazonS3 client = ((S3FileSystem)fs).getClient();
    	doReturn(true).when(client).doesBucketExist("bucket");
    	List<Bucket> buckets = new ArrayList<Bucket>();
    	buckets.add(new Bucket("bucketA"));
    	buckets.add(new Bucket("bucketB"));
		doReturn(buckets).when(client).listBuckets();
	}
	
	@After
	public void closeMemory() throws IOException{
//		fsMem.close();
	}
	
	
	private void mockFileSystem(final Path memoryBucket){
		try {
			AmazonS3ClientMock clientMock = new AmazonS3ClientMock(memoryBucket);
			S3FileSystem s3ileS3FileSystem = new S3FileSystem(provider, "mockS3Fs", clientMock, "endpoint");
			doReturn(s3ileS3FileSystem).when(provider).createFileSystem(any(URI.class), (Properties) anyObject());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    @Test
    public void getPathFirst() {
        assertEquals(fs.getPath("/bucket"),
                fs.getPath("/bucket"));

        assertEquals(fs.getPath("file"),
                fs.getPath("file"));
    }

    @Test
    public void getPathFirstWithMultiplesPaths() {
        assertEquals(fs.getPath("/bucket/path/to/file"),
            fs.getPath("/bucket/path/to/file"));
        assertNotEquals(fs.getPath("/bucket/path/other/file"),
                fs.getPath("/bucket/path/to/file"));

        assertEquals(fs.getPath("dir/path/to/file"),
                fs.getPath("dir/path/to/file"));
        assertNotEquals(fs.getPath("dir/path/other/file"),
                fs.getPath("dir/path/to/file"));
    }

    @Test
    public void getPathFirstAndMore() {
        Path actualAbsolute = fs.getPath("/bucket", "dir", "file");
        assertEquals(fs.getPath("/bucket", "dir", "file"), actualAbsolute);
        assertEquals(fs.getPath("/bucket/dir/file"), actualAbsolute);

        Path actualRelative = fs.getPath("dir", "dir", "file");
        assertEquals(fs.getPath("dir", "dir", "file"), actualRelative);
        assertEquals(fs.getPath("dir/dir/file"), actualRelative);
    }

    @Test
    public void getPathFirstAndMoreWithMultiplesPaths() {
        Path actual = fs.getPath("/bucket", "dir/file");
        assertEquals(fs.getPath("/bucket", "dir/file"), actual);
        assertEquals(fs.getPath("/bucket/dir/file"), actual);
        assertEquals(fs.getPath("/bucket", "dir", "file"), actual);
    }

    @Test
    public void getPathFirstWithMultiplesPathsAndMoreWithMultiplesPaths() {
        Path actual = fs.getPath("/bucket/dir", "dir/file");
        assertEquals(fs.getPath("/bucket/dir", "dir/file"), actual);
        assertEquals(fs.getPath("/bucket/dir/dir/file"), actual);
        assertEquals(fs.getPath("/bucket", "dir", "dir", "file"), actual);
        assertEquals(fs.getPath("/bucket/dir/dir", "file"), actual);
    }

    @Test
    public void getPathRelativeAndAbsoulte() {
        assertNotEquals(fs.getPath("/bucket"), fs.getPath("bucket"));
        assertNotEquals(fs.getPath("/bucket/dir"), fs.getPath("bucket/dir"));
        assertNotEquals(fs.getPath("/bucket", "dir"), fs.getPath("bucket", "dir"));
        assertNotEquals(fs.getPath("/bucket/dir", "dir"), fs.getPath("bucket/dir", "dir"));
        assertNotEquals(fs.getPath("/bucket", "dir/file"), fs.getPath("bucket", "dir/file"));
        assertNotEquals(fs.getPath("/bucket/dir", "dir/file"), fs.getPath("bucket/dir", "dir/file"));
    }

    @Test
    public void duplicatedSlashesAreDeleted() {
        Path actualFirst = fs.getPath("/bucket//file");
        assertEquals(fs.getPath("/bucket/file"), actualFirst);
        assertEquals(fs.getPath("/bucket", "file"), actualFirst);

        Path actualFirstAndMore = fs.getPath("/bucket//dir", "dir//file");
        assertEquals(fs.getPath("/bucket/dir/dir/file"), actualFirstAndMore);
        assertEquals(fs.getPath("/bucket", "dir/dir/file"), actualFirstAndMore);
        assertEquals(fs.getPath("/bucket/dir", "dir/file"), actualFirstAndMore);
        assertEquals(fs.getPath("/bucket/dir/dir", "file"), actualFirstAndMore);
    }


    @Test
	public void readOnlyAlwaysFalse(){
		assertTrue(!fs.isReadOnly());
	}
	
	@Test
	public void getSeparatorSlash(){
		assertEquals("/", fs.getSeparator());
		assertEquals("/", S3Path.PATH_SEPARATOR);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void getPathMatcherThrowException(){
		fs.getPathMatcher("");
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void getUserPrincipalLookupServiceThrowException(){
		fs.getUserPrincipalLookupService();
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void newWatchServiceThrowException() throws Exception {
		fs.newWatchService();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getPathWithoutBucket() {
		fs.getPath("//path/to/file");
	}
	
	@Test
	public void getFileStores(){
		Iterable<FileStore> result = fs.getFileStores();
		assertNotNull(result);
		Iterator<FileStore> iterator = result.iterator();
		assertNotNull(iterator);
		assertTrue(iterator.hasNext());
		assertNotNull(iterator.next());
	}
	
	@Test
	public void getRootDirectoriesReturnBuckets() {
		
		Iterable<Path> paths = fs.getRootDirectories();
		
		assertNotNull(paths);
		
		int size = 0;
		boolean bucketNameA = false;
		boolean bucketNameB = false;
		
		for (Path path : paths) {
			String name = path.getFileName().toString();
			if (name.equals("bucketA")) {
				bucketNameA = true;
			}
			else if (name.equals("bucketB")) {
				bucketNameB = true;
			}
			size++;
		}
		
		assertEquals(2, size);
		assertTrue(bucketNameA);
		assertTrue(bucketNameB);
	}
	
	@Test
	public void supportedFileAttributeViewsReturnBasic(){
		Set<String> operations = fs.supportedFileAttributeViews();
		
		assertNotNull(operations);
		assertTrue(!operations.isEmpty());
		
		for (String operation: operations){
			assertEquals("basic", operation);
		}
	}
	
	@Test
	public void getRootDirectories(){
		fs.getRootDirectories();
	}
	
	@Test
	public void close() throws IOException {
		assertTrue(fs.isOpen());
		fs.close();
		assertTrue(!fs.isOpen());
	}
	
	private Map<String, ?> buildFakeEnv(){
		return ImmutableMap.<String, Object> builder()
				.put(ACCESS_KEY, "access key")
				.put(SECRET_KEY, "secret key").build();
	}

    private static void assertNotEquals(Object a, Object b){
        assertTrue(a + " are not equal to: " + b, !a.equals(b));
    }
}
