package at.yomi.service;

import at.yomi.Config;
import at.yomi.Inject;
import at.yomi.Lazy;

import java.util.List;
import java.util.Map;

public class Bean {
    @Inject
    public AdditionService additionService;

    @Inject
    public Lazy<AdditionService> lazyAdditionService;

    @Config(required = true)
    public Integer value;

    @Config
    public String stringValue;

    @Config
    public Integer[] arrayValue;

    @Config
    public List<String> listValue;

    @Config
    public Map<String, String> mapValue;
}
