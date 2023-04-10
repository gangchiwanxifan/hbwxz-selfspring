package my.self.spring.beanFactory;

import my.self.spring.annotation.ComponentScan;
import my.self.spring.annotation.Scope;
import my.self.spring.annotation.Service;
import my.self.spring.beanDefinition.AnnotateBeanDefinition;
import my.self.spring.beanDefinition.AnnotateGenericBeanDefinition;
import my.self.spring.beanDefinition.BeanDefinitionRegistry;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory implements BeanDefinitionRegistry, BeanFactory {
    private final Map<String, AnnotateBeanDefinition> beanDefinitionMap =
            new ConcurrentHashMap<>(256);
    private List<String> beanDefinitionNames = new ArrayList<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    @Override
    public void registerBeanDefinition(String beanName, AnnotateBeanDefinition beanDefinition) {
        // 源码里有一些逻辑
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    public void doScan() {
        System.out.println("---");
        for (String beanName : beanDefinitionMap.keySet()) {
            AnnotateGenericBeanDefinition bd = (AnnotateGenericBeanDefinition) beanDefinitionMap.get(beanName);
            if (bd.getClazz().isAnnotationPresent(ComponentScan.class)) {
                ComponentScan componentScan = (ComponentScan) bd.getClazz().getAnnotation(ComponentScan.class);
                String basePackage = componentScan.value();
                URL resource = this.getClass().getClassLoader().getResource(basePackage.replace(".", "/"));
                File file = new File(resource.getFile());
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        try {
                            Class clazz = this.getClass()
                                    .getClassLoader()
                                    .loadClass(basePackage.concat(".").concat(f.getName().split("\\.")[0]));
                            if (clazz.isAnnotationPresent(Service.class)) {
                                String name = ((Service) clazz.getAnnotation(Service.class)).value();
                                AnnotateGenericBeanDefinition abd = new AnnotateGenericBeanDefinition();
                                abd.setClazz(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    abd.setScope(((Scope) clazz.getAnnotation(Scope.class)).value());
                                } else {
                                    abd.setScope("singleton");
                                }
                                beanDefinitionMap.put(name, abd);
                                // 需要有一个地方，记录真正的我们定义的bean
                                beanDefinitionNames.add(name);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        System.out.println("00");
    }

    // 只有我们的bean都注册上以后，才能有getBean
    @Override
    public Object getBean(String beanName) {
        return doGetBean(beanName);
    }

    private Object doGetBean(String beanName) {
        Object bean = singletonObjects.get(beanName);
        if (bean != null) return bean;
        // 需要跟据beanDefinition创建bean
        AnnotateGenericBeanDefinition bd
                = (AnnotateGenericBeanDefinition) beanDefinitionMap.get(beanName);
        Object cBean = createBean(beanName, bd);
        if (bd.getScope().equals("singleton")) {
            // createBean方法其实是完成了beanDefinition转真正的 实体对象的地方
            singletonObjects.put(beanName, cBean);
        }
        return cBean;
    }

    private Object createBean(String beanName, AnnotateGenericBeanDefinition bd) {
        try {
            return bd.getClazz().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void preInstantiateSingleton() {
        // 初始化我们定义的bean，我们就需要找到所有的 我们自定义的 beanname

        // 为什么不直接使用我们的beanDefinitions，是否多此一举
        // beanDefinitionNames处于一个并发环境中，因为我们还有beanDefinitionNames.add的逻辑
        // 如果直接使用beanDefinitionNames进行for循环，那么循环过程中，如果一旦出现其他的线程访问了
        // 我们的 有关beanDefinitonName add元素的方法，就会导致for循环失败（modCount）
        // 所以，我们此处的代码，就是备份了一个新的beanNames对象，防止beanDefinitionNames
        // 并发环境下的add的操作
        List<String> beanNames = new ArrayList<>(beanDefinitionNames);
        for (String beanName : beanDefinitionNames) {
            // 如果扫描后，有新的 通过 动态创建的 标有单例bean的Class加载到JVM
            // 这部分就会被遗漏
            AnnotateGenericBeanDefinition bd
                    = (AnnotateGenericBeanDefinition) beanDefinitionMap.get(beanName);
            if (bd.getScope().equals("singleton")) {
                // 创建单例对象，然后把这个单例对象保存到 单例池（内存缓存）里面
                // getBean方法里边就包含了 创建对象，然后放到singletonObjects里

                // 为了确保，我们进行getBean调用的时候，能够不遗漏应该初始化的单例bean
                // 所以我们把这部分逻辑放到getBean里
                getBean(beanName);
            }
        }
    }
}
