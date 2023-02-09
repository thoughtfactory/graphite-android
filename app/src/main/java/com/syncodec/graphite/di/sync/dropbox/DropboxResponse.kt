package com.syncodec.graphite.di.sync.dropbox


data class DbxToken(val accessToken : String, val refreshToken : String)

sealed class DropboxResponse {
	object Init : DropboxResponse()
	class Success<T>(val result : T? = null) : DropboxResponse()
	object Loading : DropboxResponse()
	class Error(val error : Throwable, val message : String) : DropboxResponse()
}

class DropboxAccessTokenExpiredException : Exception("Dropbox access token has expired.")
class DropboxRefreshTokenExpiredException : Exception("Dropbox refresh token has expired.")
class DropboxUnknownException : Exception("Unknown Dropbox exception.")
