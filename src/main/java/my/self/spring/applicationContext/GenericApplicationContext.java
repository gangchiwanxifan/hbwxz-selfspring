package my.self.spring.applicationContext;

import my.self.spring.beanDefinition.AnnotateBeanDefinition;
import my.self.spring.beanDefinition.BeanDefinitionRegistry;
import my.self.spring.beanFactory.DefaultListableBeanFactory;

public class GenericApplicationContext implements BeanDefinitionRegistry {

    private DefaultListableBeanFactory beanFactory;

    public GenericApplicationContext() {
        this.beanFactory = new DefaultListableBeanFactory();
    }

    @Override
    public void registerBeanDefinition(String beanName, AnnotateBeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }
}
