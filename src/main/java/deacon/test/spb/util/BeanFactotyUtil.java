package deacon.test.spb.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;

@Service
public class BeanFactotyUtil implements BeanFactoryAware {

	private static BeanFactory beanFactory = null;

	@SuppressWarnings("static-access")
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
		return (T) beanFactory.getBean(beanName);
	}
	
}
