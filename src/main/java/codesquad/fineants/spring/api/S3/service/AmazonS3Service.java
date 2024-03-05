package codesquad.fineants.spring.api.S3.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3Service {
	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Transactional
	public String upload(MultipartFile multipartFile) {
		File file = convertMultiPartFileToFile(multipartFile).orElseThrow(
			() -> new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL));
		// random file name
		String key = UUID.randomUUID() + file.getName();
		// put S3
		amazonS3.putObject(new PutObjectRequest(bucketName, key, file).withCannedAcl(
			CannedAccessControlList.PublicRead));
		// get S3
		String path = amazonS3.getUrl(bucketName, key).toString();
		// delete object
		boolean delete = file.delete();
		log.info("임시 저장 파일의 삭제 결과 : {}", delete);
		return path;
	}

	private Optional<File> convertMultiPartFileToFile(MultipartFile file) {
		if (file.getSize() > 2 * 1024 * 1024) {
			throw new BadRequestException(MemberErrorCode.IMAGE_SIZE_EXCEEDED);
		}
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			throw new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL);
		}

		return Optional.of(convertedFile);
	}

	public void deleteFile(String url) {
		try {
			String fileName = extractFileName(url);
			amazonS3.deleteObject(bucketName, fileName);
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
		}
	}

	// URL에서 파일 이름 추출하는 메소드
	private String extractFileName(String url) {
		// 예시: https://fineants.s3.ap-northeast-2.amazonaws.com/9d07ee41-4404-414b-9ee7-12616aa6bedcprofile.jpeg
		int lastSlashIndex = url.lastIndexOf('/');
		return url.substring(lastSlashIndex + 1);
	}
}
