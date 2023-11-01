package com.syncodec.graphite.di.cloud.dropbox

import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.Metadata


sealed class DropboxOperation {

	sealed class ActiveOperationResult : DropboxOperation() {
		/**
		 * [Success] : When the file is uploaded successfully
		 *
		 * [Error] : When there is an error while uploading the file
		 *
		 * [Error.UploadError] : When there is an error while uploading the file
		 *
		 * [Error.CredentialsError] : When the credentials are invalid. Usually when the access token is expired
		 *
		 * [Error.NetworkError] : When there is a network error. Retry after some time
		 *
		 * [Error.UnknownError] : When there is an unknown error. Don't retry
		 */
		sealed class UploadFileResult : ActiveOperationResult() {
			data class Success(val metadata: com.dropbox.core.v2.files.FileMetadata) : UploadFileResult()
			sealed class Error(open val exception: Exception?) : UploadFileResult() {
				data object UploadError : Error(exception = Exception("Upload error"))
				data object CredentialsError : Error(exception = Exception("Credentials error"))
				data object NetworkError : Error(exception = Exception("Network error"))
				data class UnknownError(override val exception: Exception? = null) : Error(exception = exception)
			}
		}

		sealed class DeleteFileResult : ActiveOperationResult() {
			data class Success(val deleteResult: DeleteResult) : DeleteFileResult()
			sealed class Error(open val exception: Exception?) : DeleteFileResult() {
				data object DeleteError : Error(exception = Exception("Delete error"))
				data object CredentialsError : Error(exception = Exception("Credentials error"))
				data object NetworkError : Error(exception = Exception("Network error"))
				data class UnknownError(override val exception: Exception? = null) : Error(exception = exception)
			}
		}
	}

	sealed class PassiveOperationResult : DropboxOperation() {
		/**
		 * [Success] : When the file is downloaded successfully
		 *
		 * [FileNotFound] : When the file is not found. Usually when the file is deleted. Don't retry
		 *
		 * [Error] : When there is an error while downloading the file
		 *
		 * [Error.DownloadError] : When there is an error while downloading the file
		 *
		 * [Error.CredentialsError] : When the credentials are invalid. Usually when the access token is expired
		 *
		 * [Error.NetworkError] : When there is a network error. Retry after some time
		 *
		 * [Error.UnknownError] : When there is an unknown error. Don't retry
		 */
		sealed class DownloadFileResult : PassiveOperationResult() {
			data class Success(val byteArray: ByteArray) : DownloadFileResult() {

				override fun hashCode(): Int {
					return byteArray.contentHashCode()
				}

				override fun equals(other: Any?): Boolean {
					if (this === other) return true
					if (javaClass != other?.javaClass) return false

					other as Success

					return byteArray.contentEquals(other.byteArray)
				}
			}

			data object FileNotFound : DownloadFileResult()

			sealed class Error(open val exception: Exception?) : DownloadFileResult() {
				data object DownloadError : Error(exception = Exception("Download error"))
				data object CredentialsError : Error(exception = Exception("Credentials error"))
				data object NetworkError : Error(exception = Exception("Network error"))
				data class UnknownError(override val exception: Exception? = null) : Error(exception = exception)
			}
		}

		sealed class ListFolderResult : PassiveOperationResult() {
			data class Success(val metadataList: List<Metadata>) : ListFolderResult()
			sealed class Error(open val exception: Exception?) : ListFolderResult() {
				data object FolderNotFound : Error(exception = Exception("Folder not found"))
				data object ListFolderError : Error(exception = Exception("List folder error"))
				data object CredentialsError : Error(exception = Exception("Credentials error"))
				data object NetworkError : Error(exception = Exception("Network error"))
				data class UnknownError(override val exception: Exception? = null) : Error(exception = exception)
			}
		}
	}
}
