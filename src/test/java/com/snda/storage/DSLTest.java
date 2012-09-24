package com.snda.storage;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.io.InputSupplier;
import com.snda.storage.core.Condition;
import com.snda.storage.core.CopyObjectRequest;
import com.snda.storage.core.CopyPartRequest;
import com.snda.storage.core.CopySource;
import com.snda.storage.core.GetObjectRequest;
import com.snda.storage.core.ListBucketCriteria;
import com.snda.storage.core.ListMultipartUploadsCriteria;
import com.snda.storage.core.ListPartsCriteria;
import com.snda.storage.core.MetadataDirective;
import com.snda.storage.core.ObjectCreation;
import com.snda.storage.core.Range;
import com.snda.storage.core.ResponseOverride;
import com.snda.storage.core.StorageService;
import com.snda.storage.core.UploadObjectRequest;
import com.snda.storage.core.UploadObjectResult;
import com.snda.storage.core.UploadPartRequest;
import com.snda.storage.core.UploadPartResult;
import com.snda.storage.core.support.InputSupplierEntity;
import com.snda.storage.fluent.FluentService;
import com.snda.storage.fluent.impl.FluentServiceImpl;
import com.snda.storage.policy.Policy;
import com.snda.storage.xml.CompleteMultipartUploadResult;
import com.snda.storage.xml.CopyObjectResult;
import com.snda.storage.xml.CopyPartResult;
import com.snda.storage.xml.CreateBucketConfiguration;
import com.snda.storage.xml.InitiateMultipartUploadResult;
import com.snda.storage.xml.ListAllMyBucketsResult;
import com.snda.storage.xml.ListBucketResult;
import com.snda.storage.xml.ListMultipartUploadsResult;
import com.snda.storage.xml.ListPartsResult;
import com.snda.storage.xml.Part;

/**
 * 
 * @author wangzijian@snda.com
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DSLTest {

	private FluentService service;
	
	@Mock
	private StorageService client;
	
	@Test
	public void listBuckets() {
		ListAllMyBucketsResult expected = mock(ListAllMyBucketsResult.class);
		when(client.listBuckets()).thenReturn(expected);
		
		ListAllMyBucketsResult actual = service.listBuckets();
		assertSame(expected, actual);
	}
	
	@Test
	public void createBucketWithDefaultLocation() {
		service.bucket("mybucket").create();
		
		verify(client).createBucket("mybucket");
	}

	@Test
	public void createBucketWithSpecifiedLocation() {
		service.bucket("mybucket").location(Location.HUADONG_1).create();
		
		verify(client).createBucket("mybucket", new CreateBucketConfiguration(Location.HUADONG_1));
	}
	
	@Test
	public void getBucketLocation() {
		when(client.getBucketLocation("mybucket")).thenReturn(Location.HUABEI_1);
		
		Location actual = service.bucket("mybucket").location().get();
		assertSame(Location.HUABEI_1, actual);
	}
	
	@Test
	public void listObjects() {
		ListBucketResult expected = mock(ListBucketResult.class);
		when(client.listObjects("mybucket", new ListBucketCriteria())).thenReturn(expected);
	
		ListBucketResult actual = service.bucket("mybucket").listObjects();
		assertSame(expected, actual);
	}
	
	@Test
	public void listObjectsWithCommonParameters() {
		ListBucketResult expected = mock(ListBucketResult.class);
		when(client.listObjects("mybucket", new ListBucketCriteria().
				withPrefix("books"))).thenReturn(expected);
		
		ListBucketResult actual = service.
				bucket("mybucket").
				prefix("books").
				listObjects();
		assertSame(expected, actual);
	}
	
	@Test
	public void listObjectsWithSpecifiedParameters() {
		ListBucketResult expected = mock(ListBucketResult.class);
		when(client.listObjects("mybucket", new ListBucketCriteria().
				withPrefix("books").
				withDelimiter("/").
				withMarker("books/m"))).thenReturn(expected);
		
		ListBucketResult actual = service.
				bucket("mybucket").
				prefix("books").
				delimiter("/").
				marker("books/m").
				listObjects();
		assertSame(expected, actual);
	}
	
	@Test
	public void listMultipartUploads() {
		ListMultipartUploadsResult expected = mock(ListMultipartUploadsResult.class);
		when(client.listMultipartUploads("mybucket", new ListMultipartUploadsCriteria())).
			thenReturn(expected);
		
		ListMultipartUploadsResult actual = service.bucket("mybucket").listMultipartUploads();
		assertSame(expected, actual);
		
	}
	
	@Test
	public void listMultipartUploadsWithCommonParameters() {
		ListMultipartUploadsResult expected = mock(ListMultipartUploadsResult.class);
		when(client.listMultipartUploads("mybucket", new ListMultipartUploadsCriteria().
				withDelimiter("/"))).thenReturn(expected);
		
		ListMultipartUploadsResult actual = service.
				bucket("mybucket").
				delimiter("/").
				listMultipartUploads();
		assertSame(expected, actual);
	}
	
	@Test
	public void listMultipartUploadsWithSpecifiedParameters() {
		ListMultipartUploadsResult expected = mock(ListMultipartUploadsResult.class);
		when(client.listMultipartUploads("mybucket", new ListMultipartUploadsCriteria().
				withDelimiter("/").
				withUploadIdMarker("ABCDEFG"))).thenReturn(expected);
		
		ListMultipartUploadsResult actual = service.
				bucket("mybucket").
				uploadIdMarker("ABCDEFG").
				delimiter("/").
				listMultipartUploads();
		assertSame(expected, actual);
	}
	
	@Test
	public void setBucketPolicy() {
		Policy policy = mock(Policy.class);
		
		service.bucket("mybucket").policy(policy).set();
		verify(client).setBucketPolicy("mybucket", policy);
	}
	
	@Test
	public void getBucketPolicy() {
		Policy expected = mock(Policy.class);
		when(client.getBucketPolicy("mybucket")).thenReturn(expected);
		
		Policy actual = service.bucket("mybucket").policy().get();
		assertSame(expected, actual);
	}
	
	@Test
	public void deleteBucketPolicy() {
		service.bucket("mybucket").policy().delete();
		
		verify(client).deleteBucketPolicy("mybucket");
	}
	
	@Test
	public void downloadObject() {
		SNDAObject expected = mock(SNDAObject.class);
		when(client.downloadObject("mybucket", "key", new GetObjectRequest())).thenReturn(expected);
		
		SNDAObject actual = service.
				bucket("mybucket").
				object("key").
				download();
		assertSame(expected, actual);
	}
	
	@Test
	public void downloadObjectWithRange() {
		SNDAObject expected = mock(SNDAObject.class);
		when(client.downloadObject("mybucket", "key", new GetObjectRequest().
				withRange(new Range(1, 50)))).thenReturn(expected);
		
		SNDAObject actual = service.
				bucket("mybucket").
				object("key").
				range(1, 50).
				download();
		assertSame(expected, actual);
	}
	
	@Test
	public void downloadObjectWithCondition() {
		SNDAObject expected = mock(SNDAObject.class);
		when(client.downloadObject("mybucket", "key", new GetObjectRequest().
				withCondition(new Condition().withIfMatch("abc")))).thenReturn(expected);
		
		SNDAObject actual = service.
				bucket("mybucket").
				object("key").
				ifMatch("abc").
				download();
		assertSame(expected, actual);
	}
	
	@Test
	public void downloadObjectWithResponseOverrided() {
		SNDAObject expected = mock(SNDAObject.class);
		when(client.downloadObject("mybucket", "key", new GetObjectRequest().
				withResponseOverride(new ResponseOverride().withCacheControl("private")))).
				thenReturn(expected);
		
		SNDAObject actual = service.
				bucket("mybucket").
				object("key").
				responseCacheControl("private").
				download();
		assertSame(expected, actual);
	}
	
	@Test
	public void headObject() {
		SNDAObjectMetadata expected = mock(SNDAObjectMetadata.class);
		when(client.headObject("mybucket", "key", new GetObjectRequest())).thenReturn(expected);
		
		SNDAObjectMetadata actual = service.
				bucket("mybucket").
				object("key").
				head();
		assertSame(expected, actual);
	}
	
	@Test
	public void headObjectWithRange() {
		SNDAObjectMetadata expected = mock(SNDAObjectMetadata.class);
		when(client.headObject("mybucket", "key", new GetObjectRequest().
				withRange(new Range(1000)))).thenReturn(expected);
		
		SNDAObjectMetadata actual = service.
				bucket("mybucket").
				object("key").
				range(1000).
				head();
		assertSame(expected, actual);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void putObject() {
		long contentLength = 1234L;
		InputSupplier<InputStream> inputSupplier = mock(InputSupplier.class);
		UploadObjectResult expected = mock(UploadObjectResult.class);
		when(client.uploadObject("mybucket", "key", new UploadObjectRequest().
				withContentMD5("YnBlWPwiOhfPiILrdrYcFg==").
				withObjectCreation(new ObjectCreation().
						withCacheControl("private").
						withContentType("video/mpeg").
						withMetadata("x-snda-meta-name-1", "value1").
						withMetadata("x-snda-meta-name-2", "value2").
						withMetadata("x-snda-meta-name-3", "value3").
						withStorageClass(StorageClass.REDUCED_REDUNDANCY)).
				withEntity(new InputSupplierEntity(contentLength, inputSupplier))
				)).thenReturn(expected);
		
		
		UploadObjectResult actual = service.
			bucket("mybucket").
			object("key").
			cacheControl("private").
			contentType("video/mpeg").
			contentMD5("YnBlWPwiOhfPiILrdrYcFg==").
			metadata("x-snda-meta-name-1", "value1").
			metadata("x-snda-meta-name-2", "value2").
			metadata("x-snda-meta-name-3", "value3").
			reducedRedundancy().
			entity(contentLength, inputSupplier).
			upload();
		assertSame(expected, actual);
	}
	
	@Test
	public void updateObject() {
		service.
			bucket("mybucket").
			object("key").
			cacheControl("public").
			metadata("x-snda-meta-name-1", "value1").
			reducedRedundancy().
			update();
		
		verify(client).copyObject(
				"mybucket", 
				"key", 
				new CopyObjectRequest().
				withCopySource(new CopySource("mybucket", "key")).
				withMetadataDirective(MetadataDirective.REPLACE).
				withObjectCreation(new ObjectCreation().
				withStorageClass(StorageClass.REDUCED_REDUNDANCY).
				withCacheControl("public").
				withMetadata("x-snda-meta-name-1", "value1")));
	}
	
	@Test
	public void copyObject() {
		CopyObjectResult expected = mock(CopyObjectResult.class);
		when(client.copyObject("mybucket", "key", new CopyObjectRequest().
				withCopySource(new CopySource("mybucket2", "other")).
				withMetadataDirective(MetadataDirective.REPLACE).
				withCopyCondition(new Condition().
						withIfMatch("1123")).
				withObjectCreation(new ObjectCreation().
						withCacheControl("private").
						withContentType("video/mpeg").
						withStorageClass(StorageClass.REDUCED_REDUNDANCY)))).thenReturn(expected);
		
		CopyObjectResult actual = service.
			bucket("mybucket").
			object("key").
			cacheControl("private").
			contentType("video/mpeg").
			reducedRedundancy().
			copySource("mybucket2", "other").
			copySourceIfMatch("1123").
			replaceMetadata().
			copy();
		assertSame(expected, actual);
	}
	
	@Test
	public void deleteObject() {
		service.bucket("mybucket").object("key").delete();
		
		verify(client).deleteObject("mybucket", "key");
	}
	
	@Test
	public void initiateMultipartUpload() {
		InitiateMultipartUploadResult expected = mock(InitiateMultipartUploadResult.class);
		when(client.initiateMultipartUpload("mybucket", "key", new ObjectCreation().
				withMetadata("x-snda-meta-name1", "value1").
				withMetadata("x-snda-meta-name2", "value2").
				withContentType("application/xml"))).thenReturn(expected);
		
		InitiateMultipartUploadResult actual = service.
				bucket("mybucket").
				object("key").
				contentType("application/xml").
				metadata("x-snda-meta-name1", "value1").
				metadata("x-snda-meta-name2", "value2").
				initiateMultipartUpload();
		assertSame(expected, actual);
	}
	
	@Test
	public void completeMultipart() {
		CompleteMultipartUploadResult result = mock(CompleteMultipartUploadResult.class);
		when(client.completeMultipartUpload(
				"mybucket", 
				"key", 
				"1234567890", 
				ImmutableList.of(
				new Part(1, "etag1"),
				new Part(2, "etag2"),
				new Part(3, "etag3")))).thenReturn(result);
		CompleteMultipartUploadResult actual = service.
				bucket("mybucket").
				object("key").
				multipartUpload("1234567890").
				part(new Part(1, "etag1")).
				part(new Part(2, "etag2")).
				part(new Part(3, "etag3")).
				complete();
		assertSame(result, actual);
	}
	
	@Test
	public void listParts() {
		ListPartsResult expected = mock(ListPartsResult.class);
		when(client.listParts("mybucket", "key", "ABCDEFG", new ListPartsCriteria())).
			thenReturn(expected);
		
		ListPartsResult actual = service.
				bucket("mybucket").
				object("key").
				multipartUpload("ABCDEFG").
				listParts();
		assertSame(expected, actual);
	}
	
	
	@Test
	public void listPartsWithParameter() {
		ListPartsResult expected = mock(ListPartsResult.class);
		when(client.listParts("mybucket", "key", "ABCDEFG", new ListPartsCriteria().
				withMaxParts(500).
				withPartNumberMarker(999))).
			thenReturn(expected);
		
		ListPartsResult actual = service.
				bucket("mybucket").
				object("key").
				multipartUpload("ABCDEFG").
				maxParts(500).
				partNumberMarker(999).
				listParts();
		assertSame(expected, actual);
	}
	
	@Test
	public void abortMultipartUpload() {
		service.
			bucket("mybucket").
			object("key").
			multipartUpload("ABCDEFG").
			abort();
		verify(client).abortMultipartUpload("mybucket", "key", "ABCDEFG");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void uploadPart() {
		InputSupplier<InputStream> inputSupplier = mock(InputSupplier.class);
		UploadPartResult expected = mock(UploadPartResult.class);
		when(client.uploadPart("mybucket", "key", "ABCDEFG", 213, new UploadPartRequest().
				withEntity(new InputSupplierEntity(255L, inputSupplier)))).
			thenReturn(expected);
		
		UploadPartResult actual = service.
				bucket("mybucket").
				object("key").
				multipartUpload("ABCDEFG").
				partNumber(213).
				entity(255, inputSupplier).
				upload();
		assertSame(expected, actual);
	}
	
	@Test
	public void copyPart() {
	 	CopyPartResult expected = mock(CopyPartResult.class);
		when(client.copyPart("mybucket", "key", "ABCDEFG", 213, new CopyPartRequest().
				withCopySource(new CopySource("mybucket2", "222")))).
			thenReturn(expected);
		
		CopyPartResult actual = service.
				bucket("mybucket").
				object("key").
				multipartUpload("ABCDEFG").
				partNumber(213).
				copySource("mybucket2", "222").
				copy();
		assertSame(expected, actual);
	}
	
	@Before
	public void setUp() {
		this.service = new FluentServiceImpl(client);
	}
}
