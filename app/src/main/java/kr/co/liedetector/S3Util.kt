package kr.co.liedetector

import android.content.Context
import android.text.TextUtils
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import java.io.File

/**
 * 2022. 05. 24.
 *
 * @author jbchoi
 */
/* 사용법
S3Util.getInstance()
      .setKeys("accessKey", "secretKey")
      .setRegion(Regions.AP_NORTHEAST_2)
      .uploadWithTransferUtility(
          this,
          "bucketName",
          "folderName",
          file,
          new TransferListener() {
              ...
          }
      );
 */
class S3Util {
    private var accessKey = "" // IAM AccessKey
    private var secretKey = "" // IAM SecretKey
    private var region // S3 Region
            : Region

    /**
     * Overloading
     */
    fun uploadWithTransferUtility(
        context: Context?,
        bucketName: String, file: File,
        listener: TransferListener?
    ) {
        this.uploadWithTransferUtility(
            context,
            bucketName, null, file, null,
            listener
        )
    }

    /**
     * Overloading
     */
    fun uploadWithTransferUtility(
        context: Context?,
        bucketName: String, folder: String?, file: File,
        listener: TransferListener?
    ) {
        this.uploadWithTransferUtility(
            context,
            bucketName, folder, file, null,
            listener
        )
    }

    /**
     * S3 파일 업로드
     *
     * @param context    Context
     * @param bucketName S3 버킷 이름(/(슬래쉬) 없이)
     * @param folder     버킷 내 폴더 경로(/(슬래쉬) 맨 앞, 맨 뒤 없이)
     * @param fileName   파일 이름
     * @param file       Local 파일 경로
     * @param listener   AWS S3 TransferListener
     */
    fun uploadWithTransferUtility(
        context: Context?,
        bucketName: String, folder: String?,
        file: File, fileName: String?,
        listener: TransferListener?
    ) {

//        require(
//            !(TextUtils.isEmpty(accessKey) || TextUtils.isEmpty(
//                secretKey
//            ))
//        ) { "AccessKey & SecretKey must be not null" }
        val awsCredentials: AWSCredentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3Client = AmazonS3Client(
            awsCredentials, region
        )
        val transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context)
            .build()
        TransferNetworkLossHandler.getInstance(context)
        val uploadObserver = transferUtility.upload(
            if (TextUtils.isEmpty(folder)) bucketName else "$bucketName/$folder",
            if (TextUtils.isEmpty(fileName)) file.name else fileName,
            file
        )
        uploadObserver.setTransferListener(listener)
    }

    /**
     * Access, Secret Key 설정
     */
    fun setKeys(accessKey: String, secretKey: String): S3Util {
        this.accessKey = accessKey
        this.secretKey = secretKey
        return this
    }

    /**
     * Access Key 설정
     */
    fun setAccessKey(accessKey: String): S3Util {
        this.accessKey = accessKey
        return this
    }

    /**
     * Secret Key 설정
     */
    fun setSecretKey(secretKey: String): S3Util {
        this.secretKey = secretKey
        return this
    }

    /**
     * Region Enum 으로 Region 설정
     */
    fun setRegion(regionName: Regions?): S3Util {
        region = Region.getRegion(regionName)
        return this
    }

    /**
     * Region Class 로 Region 설정
     */
    fun setRegion(region: Region): S3Util {
        this.region = region
        return this
    }

    private object LHolder {
        val instance = S3Util()
    }

    companion object {
        /**
         * Singleton Pattern
         */
        val instance: S3Util
            get() = LHolder.instance
    }

    /**
     * 생성자 생성 시 초기 Region 설정 : AP_NORTHEAST_2
     */
    init {
        region = Region.getRegion(Regions.AP_NORTHEAST_2)
    }
}