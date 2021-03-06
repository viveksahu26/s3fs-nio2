package org.carlspring.cloud.storage.s3fs;

import org.carlspring.cloud.storage.s3fs.util.EnvironmentBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.Before;
import org.junit.Test;
import static java.util.UUID.randomUUID;
import static org.carlspring.cloud.storage.s3fs.AmazonS3Factory.*;
import static org.carlspring.cloud.storage.s3fs.util.EnvironmentBuilder.getRealEnv;
import static org.junit.Assert.assertNotNull;

public class AmazonS3ClientIT
{

    AmazonS3 client;


    @Before
    public void setup()
    {
        // s3client
        final Map<String, Object> env = getRealEnv();

        BasicAWSCredentials credentialsS3 = new BasicAWSCredentials(env.get(ACCESS_KEY).toString(),
                                                                    env.get(SECRET_KEY).toString());

        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentialsS3);

        client = AmazonS3ClientBuilder.standard()
                                      .withCredentials(credentialsProvider)
                                      .withRegion(env.get(REGION).toString()).build();
    }

    @Test
    public void putObject()
            throws IOException
    {
        Path file = Files.createTempFile("file-se", "file");

        Files.write(file, "content".getBytes(), StandardOpenOption.APPEND);

        PutObjectResult result = client.putObject(getBucket(), randomUUID().toString(), file.toFile());

        assertNotNull(result);
    }

    @Test
    public void putObjectWithEndSlash()
            throws IOException
    {
        Path file = Files.createTempFile("file-se", "file");

        Files.write(file, "content".getBytes(), StandardOpenOption.APPEND);

        PutObjectResult result = client.putObject(getBucket(), randomUUID().toString() + "/", file.toFile());

        assertNotNull(result);
    }

    @Test
    public void putObjectWithStartSlash()
            throws IOException
    {
        Path file = Files.createTempFile("file-se", "file");

        Files.write(file, "content".getBytes(), StandardOpenOption.APPEND);

        client.putObject(getBucket(), "/" + randomUUID().toString(), file.toFile());
    }

    @Test
    public void putObjectWithBothSlash()
            throws IOException
    {
        Path file = Files.createTempFile("file-se", "file");

        Files.write(file, "content".getBytes(), StandardOpenOption.APPEND);

        PutObjectResult result = client.putObject(getBucket(), "/" + randomUUID().toString() + "/", file.toFile());

        assertNotNull(result);
    }

    @Test
    public void putObjectByteArray()
    {
        PutObjectResult result = client.putObject(getBucket(),
                                                  randomUUID().toString(),
                                                  new ByteArrayInputStream("contenido1".getBytes()),
                                                  new ObjectMetadata());

        assertNotNull(result);
    }

    private String getBucket()
    {
        return EnvironmentBuilder.getBucket().replace("/", "");
    }

}
