package com.vduzzle.QuizApp.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.vduzzle.QuizApp.dbo.File;
import com.vduzzle.QuizApp.repo.FileRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileService implements IFileService {

    private final FileRepo fileRepo;
    private AmazonS3 amazonS3Client;

    @Value("${aws.s3.acces.key}")
    private String accessKey;

    @Value("${aws.s3.secret.key}")
    private String secretKey;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public FileService(FileRepo fileRepo) {
        this.fileRepo = fileRepo;
    }

    @PostConstruct
    public void initS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
//        this.amazonS3Client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .withRegion(Regions.EU_NORTH_1)
//                .build();
    }

    @Override
    public File saveFile(MultipartFile file, String name) {
        String saveFilePath;
        try {
            saveFilePath = saveFileToAWSS3Bucket(file);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }
        if (saveFilePath.isEmpty()) {
            throw new RuntimeException("Failed to save file to AWS S3.");
        }

        File fileToSave = File.builder()
                .path(saveFilePath)
                .name(name)
                .build();
        return fileRepo.save(fileToSave);
    }

    @Override
    public File loadCorrespondingImage(Long imageID) {
        return null;
//        return fileRepo.findById(imageID);
    }

    private String saveFileToAWSS3Bucket(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata);
            amazonS3Client.putObject(putObjectRequest);
            return "https://"+bucketName+".s3.amazonaws.com/"+fileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
