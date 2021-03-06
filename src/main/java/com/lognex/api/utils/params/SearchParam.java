package com.lognex.api.utils.params;

public class SearchParam extends ApiParam {
    private final String value;

    private SearchParam(String value) {
        super(Type.search);
        this.value = value;
    }

    public static SearchParam search(String value) {
        if (value == null) return null;
        return new SearchParam(value);
    }

    @Override
    protected String render() {
        return value;
    }
}
