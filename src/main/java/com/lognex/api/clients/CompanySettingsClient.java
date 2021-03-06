package com.lognex.api.clients;

import com.lognex.api.LognexApi;
import com.lognex.api.clients.endpoints.GetEndpoint;
import com.lognex.api.clients.endpoints.MetadataEndpoint;
import com.lognex.api.entities.MetaEntity;
import com.lognex.api.responses.CompanySettingsResponse;
import com.lognex.api.responses.metadata.CompanySettingsMetadata;

public final class CompanySettingsClient
        extends ApiClient
        implements GetEndpoint<CompanySettingsResponse>,
        MetadataEndpoint<CompanySettingsMetadata> {

    public CompanySettingsClient(LognexApi api) {
        super(api, "/entity/companysettings/");
    }

    @Override
    public Class<? extends MetaEntity> entityClass() {
        return CompanySettingsResponse.class;
    }

    @Override
    public Class<? extends MetaEntity> metaEntityClass() {
        return CompanySettingsMetadata.class;
    }
}
