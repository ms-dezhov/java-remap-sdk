package com.lognex.api;

import com.lognex.api.entities.GlobalMetadataEntity;
import com.lognex.api.entities.MetaEntity;
import com.lognex.api.entities.UomEntity;
import com.lognex.api.entities.agents.CounterpartyEntity;
import com.lognex.api.responses.ListEntity;
import com.lognex.api.utils.LognexApiException;
import com.lognex.api.utils.RequestLogHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.lognex.api.utils.params.ExpandParam.expand;
import static com.lognex.api.utils.params.FilterParam.FilterType.equivalence;
import static com.lognex.api.utils.params.FilterParam.*;
import static com.lognex.api.utils.params.LimitParam.limit;
import static com.lognex.api.utils.params.OffsetParam.offset;
import static com.lognex.api.utils.params.OrderParam.Direction.asc;
import static com.lognex.api.utils.params.OrderParam.Direction.desc;
import static com.lognex.api.utils.params.OrderParam.order;
import static com.lognex.api.utils.params.SearchParam.search;
import static org.junit.Assert.*;

public class ApiParamsTest {
    private LognexApi api;
    private RequestLogHttpClient logHttpClient;
    private String host;

    @Before
    public void init() {
        logHttpClient = new RequestLogHttpClient();
        api = new LognexApi(
                System.getenv("API_HOST"),
                true, System.getenv("API_LOGIN"),
                System.getenv("API_PASSWORD"),
                logHttpClient
        );

        host = api.getHost();
    }

    @After
    public void antiLimits() throws InterruptedException {
        Thread.sleep(1500); // Защита от лимитов
    }

    @Test
    public void test_expand() throws IOException, LognexApiException {
        /*
            Без expand
         */

        ListEntity<CounterpartyEntity> listWithoutExpand = api.entity().counterparty().get();
        CounterpartyEntity elementWithoutExpand = listWithoutExpand.getRows().get(0);
        assertNull(elementWithoutExpand.getGroup().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            С expand
         */

        ListEntity<CounterpartyEntity> listWithExpand = api.entity().counterparty().get(expand("group"));
        CounterpartyEntity elementWithExpand = listWithExpand.getRows().get(0);
        assertNotNull(elementWithExpand.getGroup().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?expand=group",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            С вложенным expand
         */

        ListEntity<CounterpartyEntity> listWithNestedExpand = api.entity().counterparty().get(expand("owner.group"));
        CounterpartyEntity elementWithNestedExpand = listWithNestedExpand.getRows().get(0);
        assertNull(elementWithNestedExpand.getGroup().getName());
        assertNotNull(elementWithNestedExpand.getOwner().getName());
        assertNotNull(elementWithNestedExpand.getOwner().getGroup().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?expand=owner.group",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_multipleExpand() throws IOException, LognexApiException {
        /*
            expand двух полей
         */

        ListEntity<CounterpartyEntity> expand2 = api.entity().counterparty().get(expand("group", "owner"));
        CounterpartyEntity expandElement2 = expand2.getRows().get(0);
        assertNotNull(expandElement2.getGroup().getName());
        assertNotNull(expandElement2.getOwner().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?expand=group%2Cowner",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            expand двух полей, одно из которых -- вложенное
         */

        ListEntity<CounterpartyEntity> expandNested = api.entity().counterparty().get(expand("group", "owner.group"));
        CounterpartyEntity expandElementNested = expandNested.getRows().get(0);
        assertNotNull(expandElementNested.getGroup().getName());
        assertNotNull(expandElementNested.getOwner().getGroup().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?expand=group%2Cowner.group",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            expand двух полей двумя отдельными параметрами
         */

        ListEntity<CounterpartyEntity> expand22 = api.entity().counterparty().get(expand("group"), expand("owner"));
        CounterpartyEntity expandElement22 = expand22.getRows().get(0);
        assertNotNull(expandElement22.getGroup().getName());
        assertNotNull(expandElement22.getOwner().getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?expand=group%2Cowner",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_filter() throws IOException, LognexApiException {
        /*
            Без фильтрации
         */

        ListEntity<CounterpartyEntity> dataWithoutFilter = api.entity().counterparty().get();
        int size = dataWithoutFilter.getMeta().getSize();
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            C фильтром равенства
         */

        ListEntity<CounterpartyEntity> dataWithEqFilter1 = api.entity().counterparty().get(
                filterEq("name", "ООО \"Поставщик\"")
        );
        assertEquals(1, dataWithEqFilter1.getRows().size());
        assertEquals("ООО \"Поставщик\"", dataWithEqFilter1.getRows().get(0).getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?filter=name" + URLEncoder.encode("=ООО \"Поставщик\"", "UTF-8"),
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            С фильтром неравенства
         */

        ListEntity<CounterpartyEntity> dataWithEqFilter2 = api.entity().counterparty().get(
                filterNot("name", "ООО \"Поставщик\"")
        );
        assertEquals(size - 1, (int) dataWithEqFilter2.getMeta().getSize());
        assertNotEquals("ООО \"Поставщик\"", dataWithEqFilter2.getRows().get(0).getName());
        assertNotEquals("ООО \"Поставщик\"", dataWithEqFilter2.getRows().get(1).getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?filter=name" + URLEncoder.encode("!=ООО \"Поставщик\"", "UTF-8"),
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            С фильтром подобия
         */

        ListEntity<CounterpartyEntity> dataWithEqFilter3 = api.entity().counterparty().get(
                filter("name", equivalence, "ООО")
        );
        assertEquals(2, dataWithEqFilter3.getRows().size());
        assertNotEquals("Розничный покупатель", dataWithEqFilter3.getRows().get(0).getName());
        assertNotEquals("Розничный покупатель", dataWithEqFilter3.getRows().get(1).getName());
        assertEquals(
                host + "/api/remap/1.1/entity/counterparty/?filter=name" + URLEncoder.encode("~ООО", "UTF-8"),
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_multipleFilter() throws IOException, LognexApiException {
        GlobalMetadataEntity metadataWithFilter = api.entity().metadata().get(
                filterEq("type", "product"),
                filterEq("type", "service"),
                filterEq("type", "demand")
        );
        assertNull(metadataWithFilter.getCounterparty());
        assertNotNull(metadataWithFilter.getProduct());
        assertNotNull(metadataWithFilter.getService());
        assertNotNull(metadataWithFilter.getDemand());
        assertEquals(
                host + "/api/remap/1.1/entity/metadata/?filter=" +
                        "type" + URLEncoder.encode("=product;", "UTF-8") +
                        "type" + URLEncoder.encode("=service;", "UTF-8") +
                        "type" + URLEncoder.encode("=", "UTF-8") + "demand",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_limitOffset() throws IOException, LognexApiException {
        ListEntity<UomEntity> uomPlain = api.entity().uom().get();
        assertEquals(58, (int) uomPlain.getMeta().getSize());
        assertEquals(25, (int) uomPlain.getMeta().getLimit());
        assertEquals(0, (int) uomPlain.getMeta().getOffset());
        assertEquals(25, uomPlain.getRows().size());
        assertEquals(
                host + "/api/remap/1.1/entity/uom/",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        ListEntity<UomEntity> uomLimit = api.entity().uom().get(limit(10));
        assertEquals(58, (int) uomLimit.getMeta().getSize());
        assertEquals(10, (int) uomLimit.getMeta().getLimit());
        assertEquals(0, (int) uomLimit.getMeta().getOffset());
        assertEquals(10, uomLimit.getRows().size());
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?limit=10",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        ListEntity<UomEntity> uomOffset = api.entity().uom().get(offset(50));
        assertEquals(58, (int) uomOffset.getMeta().getSize());
        assertEquals(25, (int) uomOffset.getMeta().getLimit());
        assertEquals(50, (int) uomOffset.getMeta().getOffset());
        assertEquals(8, uomOffset.getRows().size());
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?offset=50",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        ListEntity<UomEntity> uomLimitOffset = api.entity().uom().get(limit(8), offset(52));
        assertEquals(58, (int) uomLimitOffset.getMeta().getSize());
        assertEquals(8, (int) uomLimitOffset.getMeta().getLimit());
        assertEquals(52, (int) uomLimitOffset.getMeta().getOffset());
        assertEquals(6, uomLimitOffset.getRows().size());
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?offset=52&limit=8",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_order() throws IOException, LognexApiException {
        ListEntity<UomEntity> uomPlain = api.entity().uom().get(limit(100));
        List<String> listAsIs = uomPlain.getRows().stream().map(MetaEntity::getName).collect(Collectors.toList());

        /*
            Сортировка по одному параметру без указания направления сортировки
         */

        ListEntity<UomEntity> uomOrderByNameAsc = api.entity().uom().get(limit(100), order("name"));
        List<String> listSortedByNameAsc = uomOrderByNameAsc.getRows().stream().map(MetaEntity::getName).collect(Collectors.toList());
        assertNotEquals(listAsIs, listSortedByNameAsc);
        assertTrue(listSortedByNameAsc.containsAll(listAsIs));
        assertTrue(listAsIs.containsAll(listSortedByNameAsc));
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?limit=100&order=name",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            Сортировка по одному параметру с указанием направления сортировки
         */

        ListEntity<UomEntity> uomOrderByNameDesc = api.entity().uom().get(limit(100), order("name", desc));
        List<String> listSortedByNameDesc = uomOrderByNameDesc.getRows().stream().map(MetaEntity::getName).collect(Collectors.toList());
        assertNotEquals(listAsIs, listSortedByNameDesc);
        assertNotEquals(listSortedByNameAsc, listSortedByNameDesc);
        assertTrue(listSortedByNameDesc.containsAll(listAsIs));
        assertTrue(listAsIs.containsAll(listSortedByNameDesc));
        assertTrue(listSortedByNameDesc.containsAll(listSortedByNameAsc));
        assertTrue(listSortedByNameAsc.containsAll(listSortedByNameDesc));

        Collections.reverse(listSortedByNameAsc);
        assertEquals(listSortedByNameAsc, listSortedByNameDesc);
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?limit=100&order=name%2Cdesc",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );

        /*
            Сортировка по нескольким параметрам
         */

        ListEntity<UomEntity> uomOrderByTwoParams = api.entity().uom().get(limit(100), order("name", desc), order("id"), order("version", asc));
        List<String> listSortedByTwoParams = uomOrderByTwoParams.getRows().stream().map(MetaEntity::getName).collect(Collectors.toList());
        assertNotEquals(listAsIs, listSortedByTwoParams);
        assertTrue(listSortedByTwoParams.containsAll(listAsIs));
        assertTrue(listAsIs.containsAll(listSortedByTwoParams));
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?limit=100&order=name%2Cdesc%3Bid%3Bversion%2Casc",
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }

    @Test
    public void test_search() throws IOException, LognexApiException {
        ListEntity<UomEntity> uomPlain = api.entity().uom().get(limit(100));
        ListEntity<UomEntity> uomSearch = api.entity().uom().get(search("мил"));

        Predicate<UomEntity> searchPredicate = o -> o.getName().toLowerCase().contains("мил") || o.getDescription().toLowerCase().contains("мил");
        assertTrue(uomSearch.getRows().stream().allMatch(searchPredicate));
        assertEquals(uomPlain.getRows().stream().filter(searchPredicate).count(), uomSearch.getRows().size());
        assertEquals(
                host + "/api/remap/1.1/entity/uom/?search=" + URLEncoder.encode("мил", "UTF-8"),
                logHttpClient.getLastExecutedRequest().getRequestLine().getUri()
        );
    }
}