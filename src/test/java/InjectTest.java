import at.yomi.Injector;
import at.yomi.service.Bean;
import at.yomi.service.DefaultAdditionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

public class InjectTest {
    private Injector injector = new Injector();

    private Bean bean;

    @Before
    public void before() {
        bean = new Bean();
        injector.start();
        injector.inject(bean);
    }

    @After
    public void after() {
        injector.stop();
    }

    @Test
    public void testInjectedService() {
        Assert.assertEquals(3, bean.additionService.add(1, 2));
    }

    @Test
    public void testIntegerConfiguration() {
        Assert.assertEquals(Integer.valueOf(1), bean.value);
    }

    @Test
    public void testStringConfiguration() {
        Assert.assertEquals("stringValue", bean.stringValue);
    }

    @Test
    public void testArrayConfiguration() {
        Assert.assertEquals(Arrays.asList(1, 2, 3), Arrays.asList(bean.arrayValue));
    }

    @Test
    public void testListConfiguration() {
        Assert.assertEquals(Arrays.asList("foo", "baz", "bar"), bean.listValue);
    }

    @Test
    public void testMapConfiguration() {
        Assert.assertEquals(new HashMap<String, String>() {{
            put("foo", "bar");
            put("baz", "boo");
        }}, bean.mapValue);
    }

    @Test
    public void testLazyService() {
        Assert.assertEquals(3, bean.lazyAdditionService.bind("DefaultAdditionService").add(1, 2));
    }

    @Test
    public void testStartAndStop() {
        DefaultAdditionService additionService = (DefaultAdditionService) bean.additionService;
        Assert.assertTrue(additionService.started);
        injector.stop();
        Assert.assertFalse(additionService.started);
    }
}
