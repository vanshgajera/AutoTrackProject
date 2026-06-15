package com.java.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

@Service
public class CRUDService {

    public String createCRUD(CRUD crud) {
//        Firestore dbFirestore = FirestoreClient.getFirestore();
//        ApiFuture<WriteResult> collectionApiFeture = dbFirestore.collection()
        return "";
    }

    public CRUD getCRUD(String documentId) {
        return null;
    }

    public String updateCRUD(CRUD crud) {
        return "";
    }

    public String deleteCRUD(String documentId) {
        return "";
    }


}
