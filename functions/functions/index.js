const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const gcs = require('@google-cloud/storage')();
const path = require('path');
const os = require('os');
const fs = require('fs');
const csv = require('csvtojson');
const request = require('request')

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//

exports.clearDatabase = functions.https.onRequest(function (request, response) {
    response.send("Completed");
    return admin.database().ref("/companies").remove();

});

exports.generateDatabaseManually = functions.https.onRequest(function (request, response) {
    response.send("Completed");
    const filePath = "companies.csv";
    const bucket = gcs.bucket("gs://tapf-hyd.appspot.com");
    const companies = admin.database().ref("/companies");
    const tempFilePath = path.join(os.tmpdir(), filePath);
    var file = bucket.file(filePath);
    var states = [];
    return file.download({
        destination: tempFilePath,
    }).then(function () {
        return csv()
            .fromFile(tempFilePath)
            .on("json", function (jsonObj) {
                if (states.indexOf(jsonObj.REGISTERED_STATE) === -1) {
                    states.push(jsonObj.REGISTERED_STATE);
                    return companies.child(jsonObj.REGISTERED_STATE)
                        .remove()
                        .then(function () {
                            companies
                                .child(jsonObj.REGISTERED_STATE)
                                .child(jsonObj.CIN)
                                .set(jsonObj);
                            return "";
                        })
                } else {
                    companies
                        .child(jsonObj.REGISTERED_STATE)
                        .child(jsonObj.CIN)
                        .set(jsonObj);
                    return "";
                }
            }).on("done", function (error) {
                fs.unlinkSync(tempFilePath);
            });
    });
});

exports.generateDatabase = functions.storage.object().onChange(function (event) {
    const filePath = "companies.csv";
    const bucket = gcs.bucket("gs://tapf-hyd.appspot.com");
    const companies = admin.database().ref("/companies");
    const tempFilePath = path.join(os.tmpdir(), filePath);
    var file = bucket.file(filePath);
    return file.download({
        destination: tempFilePath,
    }).then(function () {
        return csv()
            .fromFile(tempFilePath)
            .on("json", function (jsonObj) {
                companies
                    .child(jsonObj.REGISTERED_STATE)
                    .child(jsonObj.CIN)
                    .set(jsonObj);
                return "";
            }).on("done", function (error) {
                fs.unlinkSync(tempFilePath);
            });
    });
});