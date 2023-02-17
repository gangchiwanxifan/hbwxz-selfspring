package my.self.spring.beanDefinition;

import my.self.spring.annotation.Scope;

public class AnnotateBeanDefinitionReader {
    private BeanDefinitionRegistry registry;

    public AnnotateBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    // 注册我们的 路径 扫描这个bean到bean工厂里
    public void register(Class<?> componentClass) {
        registerBean(componentClass);
    }

    private void registerBean(Class<?> componentClass) {
        doRegisterBean(componentClass);
    }

    private void doRegisterBean(Class<?> componentClass) {
        // 把 appConfig类 读成一个 BeanDefinition 定义
        AnnotateGenericBeanDefinition beanDefinition
                = new AnnotateGenericBeanDefinition();
        beanDefinition.setClazz(componentClass);
        if (componentClass.isAnnotationPresent(Scope.class)) {
            String scope = componentClass.getAnnotation(Scope.class).value();
            beanDefinition.setScope(scope);
        } else {
            beanDefinition.setScope("singleton");
        }
        // beanDefinition创建完成后，得给beanFactory 进行bean注册了
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinition, this.registry);

    }
}
