const functions = require("firebase-functions");
const admin = require('firebase-admin');
const crypto = require('crypto');
const dropbox = require('dropbox');
const fetch = require('node-fetch');

// // Create and deploy your first functions
// // https://firebase.google.com/docs/functions/get-started
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

admin.initializeApp();

exports.deleteUser = functions.https.onRequest(
    (request, response) => {
        const data = {
            uId: request.query.uId
        };

        admin.auth().deleteUser(data.uId);
    }
);

exports.dropboxCallback = functions.https.onRequest(
    (request, response) => {
        const code = request.query.code;

        response.redirect(`intent://graphite.syncodec.com/dropboxCallback?code=${code}#Intent;scheme=https;package=com.syncodec.graphite;end`);
    }
);

exports.submitBugReport = functions.https.onRequest(
    (request, response) => {

        const data = {
            "bugType": request.body.data.bugType,
            "bugComponent": request.body.data.bugComponent,
            "title": request.body.data.title,
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

exports.exchangeDropboxCodeForToken = functions.https.onRequest(
    (request, response) => {
        // const code = request.body.data.code;
        const code = request.query.code;
        console.log(code);

        var dbxAuth = new dropbox.DropboxAuth();

        dbxAuth.setClientId('wqgzkie6sm7xxvw');
        dbxAuth.setClientSecret('kgu8ymntbtwnsxc');

        // const redirectUri = `intent://graphite.syncodec.com/dropboxCallback#Intent;scheme=https;package=com.syncodec.graphite;end`
        // const redirectUri = `http://localhost:5001/graphite-diary/us-central1/dropboxCallback`
        const redirectUri = `http://localhost:5001/graphite-diary/us-central1/dropboxCallback`
        dbxAuth.getAccessTokenFromCode(redirectUri, code)
            .then((token) => {
                response.status(200).send({
                    "code": code
                });
            })
            .catch((reason) => {
                response.status(200).send({
                    "code": code,
                    "reason": reason
                });
            });
    }
);