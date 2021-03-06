package com.lognex.api.clients.documents;

import com.lognex.api.LognexApi;
import com.lognex.api.clients.ApiClient;
import com.lognex.api.clients.endpoints.DocumentMetadataEndpoint;
import com.lognex.api.clients.endpoints.GetListEndpoint;
import com.lognex.api.clients.endpoints.MetadataAttributeEndpoint;
import com.lognex.api.clients.endpoints.PostEndpoint;
import com.lognex.api.entities.MetaEntity;
import com.lognex.api.entities.documents.CashOutDocumentEntity;
import com.lognex.api.responses.metadata.MetadataAttributeSharedStatesResponse;

public final class DocumentCashOutClient
        extends ApiClient
        implements
        GetListEndpoint<CashOutDocumentEntity>,
        PostEndpoint<CashOutDocumentEntity>,
        DocumentMetadataEndpoint<MetadataAttributeSharedStatesResponse>,
        MetadataAttributeEndpoint {

    public DocumentCashOutClient(LognexApi api) {
        super(api, "/entity/cashout/");
    }

    @Override
    public Class<? extends MetaEntity> entityClass() {
        return CashOutDocumentEntity.class;
    }

    @Override
    public Class<? extends MetaEntity> metaEntityClass() {
        return MetadataAttributeSharedStatesResponse.class;
    }
}
