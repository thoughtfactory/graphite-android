const functions = require("firebase-functions");
const admin = require('firebase-admin');
const crypto = require('crypto');
const dropbox = require('dropbox');
const fetch = require('node-fetch');

admin.initializeApp();

// const BASE_ADDRESS = "https://f22d-2001-1970-5d1f-d000-00-dcfb.ngrok.io";
const BASE_ADDRESS = "https://us-central1-graphite-diary.cloudfunctions.net";
// const BASE_ADDRESS = "http://localhost:5001";

exports.deleteAccount = functions.https.onRequest(
    (request, response) => {
        const data = {
            uId: request.body.data.uId
        };

        console.log(data.uId);

        const docRef = admin.firestore().collection('toDelete').doc(data.uId);

        docRef.set({
                timestamp: admin.firestore.Timestamp.now(),
            }).then((result => {
                response.status(200).send({
                    "data": "Ok"
                });
            }))
            .finally(() => {
                response.status(200).send({
                    "data": "Error"
                });
            });
    }
);

exports.submitBugReport = functions.https.onRequest(
    (request, response) => {

        const data = {
            "bugType": request.body.data.bugType,
            "bugComponent": request.body.data.bugComponent,
            "title": request.body.data.title,
            "email": request.body.data.email,
            "description": request.body.data.description
        };

        try {
            admin.storage().bucket().file("feedback/" + crypto.randomUUID() + ".json").save(JSON.stringify(data));
            response.status(200).send({
                "data": "Ok"
            });
        } catch (error) {
            response.status(200).send({
                "data": "Ok"
            });
        }
    }
);

exports.dropboxCallback = functions.https.onRequest(
    (request, response) => {
        const code = request.query.code;

        response.redirect(`intent://graphite.syncodec.com/dropboxCallback?code=${code}#Intent;scheme=https;package=com.syncodec.graphite;end`);
    }
);

exports.connectWithDropbox = functions.https.onRequest(
    (request, response) => {
        const FIREBASE_FUNCTION_PATH = "dropboxCallback";

        var dbxAuth = new dropbox.DropboxAuth();
        dbxAuth.setClientId('wqgzkie6sm7xxvw');
        dbxAuth.getAuthenticationUrl(
                redirectUri = encodeURIComponent(`${BASE_ADDRESS}/${FIREBASE_FUNCTION_PATH}`),
                state = null,
                authType = `code`,
                tokenAccessType = "offline",
                scope = null,
                includeGrantedScope = `none`,
                isePKCE = false,
            ).then((authUrl) => {
                console.log(`authUrl : ${authUrl}`);
                response.redirect(authUrl);
            })
            .catch((reason) => {
                response.status(200).send({
                    "response": "Error"
                });
            });
    }
);

exports.dropboxExchangeCodeForToken2 = functions.https.onRequest(
    (request, response) => {
        const code = request.body.data.code;

        const FIREBASE_FUNCTION_PATH = "dropboxCallback";

        var dbxAuth = new dropbox.DropboxAuth();

        dbxAuth.setClientId('wqgzkie6sm7xxvw');
        dbxAuth.setClientSecret('kgu8ymntbtwnsxc');

        dbxAuth.getAccessTokenFromCode(encodeURIComponent(`${BASE_ADDRESS}/${FIREBASE_FUNCTION_PATH}`), code).then((dropboxResponse) => {
                response.status(200).send({
                    "data": {
                        "response": "Ok",
                        "data": dropboxResponse.result
                    },
                });
            })
            .catch((reason) => {
                console.log(reason)
                response.status(200).send({
                    "data": {
                        "response": "Error",
                        "data": reason
                    }
                });
            });
    }
);

exports.dropboxExchangeRefreshTokenForAccessToken = functions.https.onRequest(
    (request, response) => {

        const refreshToken = request.body.data.refreshToken;

        var headers = new fetch.Headers();
        headers.append("Content-Type", "application/x-www-form-urlencoded");
        headers.append("Cookie", "locale=en; t=jLaM3dKpDM890kqfV70PHFN7");

        var body = new URLSearchParams();
        body.append("grant_type", "refresh_token");
        body.append("refresh_token", refreshToken);
        body.append("client_id", "wqgzkie6sm7xxvw");
        body.append("client_secret", "kgu8ymntbtwnsxc");

        var requestOptions = {
            method: 'POST',
            headers: headers,
            body: body,
            redirect: 'follow'
        };

        fetch("https://api.dropbox.com/oauth2/token", requestOptions)
            .then(response => response.text())
            .then(result => {
                response.status(200).send({
                    "data": {
                        "result": result,
                        "response": "Ok"
                    }
                });
            })
            .catch(error => {
                response.status(200).send({
                    "data": {
                        "error": error,
                        "response": "Error"
                    }
                });
            });
    }
);