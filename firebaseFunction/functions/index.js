const functions = require('firebase-functions');
const admin = require('firebase-admin');
const googleapis = require('googleapis');
const crypto = require('crypto');

const key = {
	"type": "service_account",
	"project_id": "sequoiater",
	"private_key_id": "8471ad7a6b3eed92c729dd889017166c96c7875c",
	"private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbt4hRGRaDGl3D\nds7/pabIkAyl0lzDlTq/ogTpkhExF04yTxAc3Q//pytUJoWMu4px3BGKUSZPybeN\nCE1ZtG76yfVYboeMS9YME2NnYbryXdUOJYX1Ul9nuqkPNyVnKvf2U1zJ0BGzQzU9\nq9AKiPfxw9xaJOJLCQH1MlTe2OhHGjBkNhV3YJxOqSIBn7ah5mKpyPfHNJqUKe/9\nJPw866/kxBSi2lFo7uWDRtp5WyWwrfnv1CIan8CMMFpb+VNXZr2BkOsryL9ZdICE\neip9cPs7gni+ZOZjE1EDZ0G/6IFKUVupcjTpbB36WaT/HE+PbKl/iBj7SfTr/2sK\nb25WyRH5AgMBAAECggEADKo6ccYlmOzYZXZ7UuWpPAojTTVf4B8kvm4HZ5aUGWG4\n4B9NbduOWo/BLBX5/2lIKFyVfKIpszbyhoup/DrQWqDPI1h8uSzN2wDqBoA3ZqZl\nFsJ3M8MgXrJwNPxE6YWK0hkTpJfCE3TK7Borpepf/Y+dk3iz2GsszUGWZOL6zsAy\nZvOM1BGvMHDgWx51bZp4rLRIX9GKWUOjrFl4AoRZFUI7rPNSQQ2LU85tznce/g1q\nf+ABNgCPrCPjeuoHjTyaoFKTgbzcg5u3fh0lMne2ATnbVewp+lyzsOTvxFyZsWla\n9yQ81+Hz2EOwr3fZ1YBCFKXXESk/yRBbgvfBSQvkUQKBgQDIBPKhT5pJxKl6EcKF\nfnMG2Deb8sqIveTXzxZGpDDq6Dna/yFX1uhIRwo0H531Hff++cWMNsrGm9D6kD3N\nMQg0d8876pWcgpHyYDSax2sqxUa6eyHGzaYbgSjzY6rELYkGkls/CUo3zqkDvxiA\nwTAMDhzI6ISfKv9lsnvZ7XSvNQKBgQDHTGQjgRipC7NRndvaWcYk1dQITk+dYYAz\n5Fx2vvU4t+0ZlKiB/0ftvmbePjkX7UlEx9zfmFgrdADwdnGvItsXiazAxPv0+6mf\nU9X6zD9kWycc1qpGMCtD767NN6SYDQ8zwGg9KGgo4sJeygC0HiIfyvo09Dn6qeRo\n41AlzHscNQKBgE1J/CdxBwFySLqumifTUMdYTUSpcLGeDwuFLepmD7D1w0VoSI4U\nsPUc3kh93x+UUPvpr/gaRAiytPHdQsRDKhR8J0vTvbQYkCTEIA+e9z4ztLziT47u\ncD04j4ZdAOOFZOlxLGkTQeaGqLCV6vX1sIc3/M91aanHh6DYcxsmsmvxAoGAZy/6\nORfCOORB9+QPzMHmgXPk0FgbiJTO2A9IgILmXHN+Y1xA0u3lxAZtJGx7CxRkdyQ0\nC+DI5dauFdJ9kfwuC8XQmA5llbYnYML7a1sNB1zap2fNyYFvJUGX89D3e3JniJf6\nYI/nPiQKFyHns1pWuLdTTM+WpV4JUPVArhFtPHECgYEAj3AO4jEEi/v9sU7wnGMo\n40OmXxdzmLOB8LBsTqLw20ZcMQodC+aqztYEd/wedj2YDhaMxietiHGgbo8mgUT2\ncj3ZtutJDAyrfynCEEomIYPugBZhjMVFfen32d3flB5mJ79bK04zFyKEkaXKHfjw\nBalxwOMd0ZVYEooBjjnm4cM=\n-----END PRIVATE KEY-----\n",
	"client_email": "sequoiater@appspot.gserviceaccount.com",
	"client_id": "109607617716499006028",
	"auth_uri": "https://accounts.google.com/o/oauth2/auth",
	"token_uri": "https://oauth2.googleapis.com/token",
	"auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
	"client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/sequoiater%40appspot.gserviceaccount.com"
};

const password = "tetramand";


admin.initializeApp();

async function validatePurchase(queryInfo) {
	return new Promise(
		(resolve, reject) => {
			const Schema$SubscriptionPurchase = googleapis.androidpublisher_v3.Schema$SubscriptionPurchase;
			const Androidpublisher = googleapis.androidpublisher_v3.Androidpublisher;

			const scopes = "https://www.googleapis.com/auth/androidpublisher";

			googleapis.google.auth.getClient({
					scopes: scopes,
					credentials: key
				}).then(auth => {
					const androidPublisher = new Androidpublisher({
						auth: auth
					});

					androidPublisher.purchases.subscriptions.get({
						packageName: queryInfo.packageName,
						token: queryInfo.purchaseToken,
						subscriptionId: queryInfo.subscriptionId
					}).then((response) => {
						var returnResponse = {
							"queryInfo": queryInfo,
							"queryResponse": response.data,
							"status": response.status
						}
						resolve(returnResponse)
					})
				})
				.catch((reason) => {
					reject(reason);
				})
		});
}

exports.storePurchase = functions.https.onRequest(
	(request, response) => {
		const queryInfo = {
			userId: request.query.userId,
			orderId: request.query.orderId,
			purchaseTime: request.query.purchaseTime,
			purchaseToken: request.query.purchaseToken,
			packageName: request.query.packageName,
			subscriptionIdList: String(request.query.subscriptionIdList).split(";").filter(item => item),
			isValid: false,
			isExpired: false
		};

		const firestore = admin.firestore();

		const validatePurchasePromises = [];

		queryInfo.subscriptionIdList.forEach((subscriptionId) => {
			var _queryInfo = queryInfo;
			_queryInfo["subscriptionId"] = subscriptionId;

			validatePurchasePromises.push(validatePurchase(_queryInfo));
		});

		Promise.all(validatePurchasePromises)
			.then((validationResponseList) => {

				const date = new Date();
				const currentTimestamp = date.getTime();
				var purchaseStorePromises = [];
				var returnResponseData = [];
				var isPremium = false;

				validationResponseList.forEach((validationResponse) => {

					const queryInfo = validationResponse.queryInfo;
					const queryResponse = validationResponse.queryResponse;
					const status = validationResponse.status;

					if (status == 200) {
						queryInfo.isValid = true;
					} else {
						queryInfo.isValid = false;
					}

					if (queryResponse.expiryTimeMillis < currentTimestamp) {
						queryInfo.isExpired = true;
					} else {
						queryInfo.isExpired = false;
						isPremium = true;

						const _returnResponseData = {
							"expiryTimeMillis": queryResponse.expiryTimeMillis,
							"subscriptionId": queryInfo.subscriptionId
						}
						returnResponseData.push(_returnResponseData)
					}

					purchaseStorePromises.push(firestore.doc(`user/${queryInfo.userId}/purchase/${queryInfo.orderId}`).set(queryInfo));

				});

				Promise.all(purchaseStorePromises)
					.then((writeResultList) => {
						response.send({
							"response": "OK",
							"responseData": returnResponseData,
							"isPremium": isPremium
						});
					})
					.catch((error) => {
						console.error(error);
						response.status(500).send({
							"response": "ERROR"
						});
					});
			})
			.catch((error) => {
				console.error(error);
				response.status(500).send({
					"response": "ERROR"
				});
			});
	}
);

exports.restorePurchase = functions.https.onRequest(
	(request, response) => {

		const queryInfo = {
			userId: request.query.userId,
			orderId: request.query.orderId,
			purchaseTime: request.query.purchaseTime,
			purchaseToken: request.query.purchaseToken,
			packageName: request.query.packageName,
			subscriptionIdList: String(request.query.subscriptionIdList).split(";").filter(item => item),
			isValid: false
		};

		var firestore = admin.firestore();

		if (queryInfo.orderId == undefined) {
			firestore.collection(`user/${queryInfo.userId}/purchase`).get()
				.then((snapshot) => {

					const transactionPromises = [];

					snapshot.docs.forEach((doc) => {
						const purchaseInfo = doc.data();

						if (purchaseInfo.isExpired != true && doc.id != "override") {
							transactionPromises.push(validatePurchase(purchaseInfo));
						}
					});

					Promise.all(transactionPromises)
						.then((responseDataList) => {
							const returnResponseData = []

							const date = new Date();
							const currentTimestamp = date.getTime();
							var purchaseUpdatePromises = [];
							var expiryTimeMillis = -1;
							var isPremium = false;

							responseDataList.forEach((responseData) => {
								var purchaseQueryInfo = responseData.queryInfo;
								const purchaseInfo = responseData.queryResponse;
								if (purchaseInfo.expiryTimeMillis < currentTimestamp) {
									purchaseQueryInfo["isExpired"] = true
									console.log(purchaseQueryInfo)
									purchaseUpdatePromises.push(firestore.doc(`user/${queryInfo.userId}/purchase/${purchaseQueryInfo.orderId}`).set(purchaseQueryInfo));
								} else {
									const _returnResponseData = {
										"expiryTimeMillis": purchaseInfo.expiryTimeMillis,
										"subscriptionId": purchaseQueryInfo.subscriptionId
									}
									isPremium = true;
									expiryTimeMillis = Math.max(purchaseInfo.expiryTimeMillis, expiryTimeMillis)
									returnResponseData.push(_returnResponseData);
								}
							});

							Promise.all(purchaseUpdatePromises)
								.then((writeResult) => {

									firestore.doc(`user/${queryInfo.userId}/purchase/override`).get()
										.then((doc) => {
											if (doc.exists) {
												var data = doc.data();
												expiryTimeMillis = data.expiryTimeMillis._seconds * 1000;
												if (expiryTimeMillis > currentTimestamp) {
													isPremium = true;
												} else {
													isPremium = false;
												}

												if (isPremium == true) {
													response.send({
														"response": "OK",
														"expiryTimeMillis": expiryTimeMillis,
														"currentTimeMillis": currentTimestamp,
														"isPremium": isPremium
													});
													console.log(`Query : ${queryInfo.userId} : override`);
												}
											} else {
												response.send({
													"response": "OK",
													"data": returnResponseData,
													"expiryTimeMillis": expiryTimeMillis,
													"currentTimeMillis": currentTimestamp,
													"isPremium": isPremium
												});
												console.log(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
											}
										});
								})
								.catch((error) => {
									console.error(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
									console.error(error);
									response.status(500).send({
										"response": "ERROR"
									});
								});
						})
						.catch((error) => {
							console.error(error);
							response.send({
								"response": "ERROR"
							});
						});
				});
		} else {
			firestore.collection(`user/${queryInfo.userId}/purchase`).get()
				.then((snapshot) => {

					var isPurchasePresent = false;

					const transactionPromises = [];
					snapshot.docs.forEach((doc) => {
						const purchaseInfo = doc.data()
						console.log(purchaseInfo)

						if (doc.id == queryInfo.orderId) {
							isPurchasePresent = true;
						}

						if (purchaseInfo.type != "override") {
							purchaseInfo.subscriptionIdList.forEach((subscriptionId) => {
								const _purchaseInfo = purchaseInfo;
								_purchaseInfo.subscriptionId = subscriptionId;

								if (_purchaseInfo.isExpired != true) {
									console.log(_purchaseInfo.userId + " : " + _purchaseInfo.isExpired)
									transactionPromises.push(validatePurchase(_purchaseInfo));
								}

							});
						}

					});

					Promise.all(transactionPromises)
						.then((responseDataList) => {

							var remainingPurchaseStorePromises = []

							if (isPurchasePresent == false) {
								remainingPurchaseStorePromises.push(firestore.doc(`user/${queryInfo.userId}/purchase/${queryInfo.orderId}`).set(queryInfo));
							}

							Promise.all(remainingPurchaseStorePromises)
								.then((writeResult) => {
									const returnResponseData = []

									const date = new Date();
									const currentTimestamp = date.getTime();
									var purchaseUpdatePromises = [];
									var expiryTimeMillis = -1;
									var isPremium = false;

									responseDataList.forEach((responseData) => {
										if (responseData.status == 200) {
											var purchaseQueryInfo = responseData.queryInfo;
											const purchaseInfo = responseData.queryResponse;
											if (purchaseInfo.expiryTimeMillis < currentTimestamp) {
												purchaseQueryInfo["isExpired"] = true
												console.log(purchaseQueryInfo)
												purchaseUpdatePromises.push(firestore.doc(`user/${queryInfo.userId}/purchase/${purchaseQueryInfo.orderId}`).set(purchaseQueryInfo));
											} else {
												const _returnResponseData = {
													"expiryTimeMillis": purchaseInfo.expiryTimeMillis,
													"subscriptionId": purchaseQueryInfo.subscriptionId
												}
												isPremium = true;
												expiryTimeMillis = Math.max(purchaseInfo.expiryTimeMillis, expiryTimeMillis)
												returnResponseData.push(_returnResponseData);
											}
										}
									});

									Promise.all(purchaseUpdatePromises)
										.then((writeResult) => {

											firestore.doc(`user/${queryInfo.userId}/purchase/override`).get()
												.then((doc) => {
													if (doc.exists) {

														var data = doc.data();
														expiryTimeMillis = data.expiryTimeMillis._seconds * 1000;
														if (expiryTimeMillis > currentTimestamp) {
															isPremium = true;
														} else {
															isPremium = false;
														}

														if (isPremium == true) {
															response.send({
																"response": "OK",
																"expiryTimeMillis": expiryTimeMillis,
																"currentTimeMillis": currentTimestamp,
																"isPremium": isPremium
															});
															console.log(`Query : ${queryInfo.userId} : override`);
														}
													} else {
														response.send({
															"response": "OK",
															"data": returnResponseData,
															"expiryTimeMillis": expiryTimeMillis,
															"currentTimeMillis": currentTimestamp,
															"isPremium": isPremium
														});
														console.log(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
													}
												});
										})
										.catch((error) => {
											console.error(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
											console.error(error);
											response.status(500).send({
												"response": "ERROR"
											});
										});
								})
						})
						.catch((error) => {
							console.error(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
							console.error(error);
							response.status(500).send({
								"response": "ERROR"
							});
						});
				})
				.catch((error) => {
					console.error(`Query : ${queryInfo.userId} : ${queryInfo.orderId}`);
					console.error(error);
					response.status(500).send({
						"response": "ERROR"
					});
				});
		}
	}
);

exports.testValidatePurchase = functions.https.onRequest(
	(request, response) => {
		const queryInfo = {
			"token": request.query.purchaseToken,
			"packageName": request.query.packageName,
			"subscriptionId": request.query.subscriptionId
		}

		const Schema$SubscriptionPurchase = googleapis.androidpublisher_v3.Schema$SubscriptionPurchase;
		const Androidpublisher = googleapis.androidpublisher_v3.Androidpublisher;

		const androidPublisherScope = "https://www.googleapis.com/auth/androidpublisher";

		googleapis.google.auth.getClient({
				scopes: androidPublisherScope,
				credentials: key
			}).then(auth => {
				const androidPublisher = new Androidpublisher({
					auth: auth
				});

				androidPublisher.purchases.subscriptions.get({
					token: queryInfo.token,
					packageName: queryInfo.packageName,
					subscriptionId: queryInfo.subscriptionId
				}).then((queryResponse) => {
					var returnResponse = {
						"queryInfo": queryInfo,
						"queryResponse": queryResponse.data,
						"status": queryResponse.status
					}
					console.log(returnResponse);
					response.send(returnResponse);
				})
			})
			.catch((error) => {
				console.log(error)
				response.status(500).send({
					"response": "ERROR"
				});
			})
	}
);

exports.storeServerAuthCode = functions.https.onRequest(
	(request, response) => {
		console.log("===========================	START	===========================");
		const queryInfo = {
			"userId": decrypt(request.body.userId),
			"refreshToken": decrypt(request.body.serverAuthCode)
		};

		console.log(queryInfo);

		const firestore = admin.firestore();

		if (queryInfo.userId != undefined && queryInfo.refreshToken != undefined) {
			firestore.doc(`user/server/${queryInfo.userId}/refreshToken`).set(queryInfo)
				.then((writeResult) => {
					const scopes = "https://www.googleapis.com/auth/drive.appdata"
					googleapis.google.auth.getClient({
							scopes: scopes,
							credentials: key
						}).then(auth => {

							auth.getAccessToken().then((token) => {


									console.log(token);
									response.send({
										"response": "OK",
										"response": token
									});
								})
								.catch((reason) => {
									response.send({
										"response": "ERROR",
										"error": "Token retrival unsuccessful.",
										"reason": reason
									});
								});
						})
						.catch((resson) => {
							response.send({
								"response": "ERROR",
								"error": "Google authorization unsuccessful.",
								"reason": reason
							});
						});
				})
				.catch((reason) => {
					response.send({
						"response": "ERROR",
						"error": "Unable to save data.",
						"reason": reason
					});
				});
		} else {
			response.send({
				"response": "ERROR",
				"error": "Request data corrupted."
			});
		}
	}
);

exports.testFunction = functions.https.onRequest(
	(request, response) => {
		const queryInfo = {
			"ciphertext": request.body.cipherText,
			"password": request.body.password
		}
		// decrypt(queryInfo.ciphertext, queryInfo.password);

		const encryptedText = encrypt(queryInfo.ciphertext, queryInfo.password);
		const decryptedText = decrypt(encryptedText, queryInfo.password)

		response.send({
			"response": "OK",
			"lol": "lol",
			"plainText": decryptedText
		});
	}
);

function decrypt(ciphertext) {
	const fields = ciphertext.split("]");

	const salt = Buffer.from(fields[0], 'base64');
	const iv = Buffer.from(fields[1], 'base64');
	const cipherBytes = Buffer.from(fields[2], 'base64');

	const passphrase = deriveKey(password, salt);

	const decipher = crypto.createDecipheriv('aes-256-cbc', passphrase, iv);
	const intermediateBuffer = decipher.update(cipherBytes, 'base64', 'utf-8').toString('utf-8');

	const decryptedText = intermediateBuffer.concat(decipher.final().toString('utf-8'));

	return decryptedText;
}

function encrypt(plaintext) {
	const salt = generateSalt();
	const iv = generateIv();

	const passphrase = deriveKey(password, salt);

	const cipher = crypto.createCipheriv('aes-256-cbc', passphrase, iv);
	const encrypted = cipher.update(plaintext);

	const finalBuffer = Buffer.concat([encrypted, cipher.final()]);
	const encryptedText = salt.toString('base64') + ']' + iv.toString('base64') + ']' + finalBuffer.toString('base64')

	return encryptedText;
}

function deriveKey(password, salt) {
	return crypto.pbkdf2Sync(password, salt, 35423, 32, 'sha1')
}

function generateSalt() {
	return crypto.randomBytes(32);
}

function generateIv() {
	return crypto.randomBytes(16);
}