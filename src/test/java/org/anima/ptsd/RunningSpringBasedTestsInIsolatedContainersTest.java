package org.anima.ptsd;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class RunningSpringBasedTestsInIsolatedContainersTest {
    
    //@Rule
    NewContainerAndConnectionRule asd = new NewContainerAndConnectionRule(); // with @Autowired context?
    //actually, if I get a fresh db everytime what about the db schema?ù
    //have to rerun hibernate each time (slow) or cache in a fresh new container before all tests

    @Test
    public void something(){
    }
    
    @Test
    public void somethingElse(){
    }
    
    public static class Config {
        
        @Bean
        public String as(){
            System.out.println("asdasdasdasd");
            return "asd";
        }
    }
    
    public static class NewContainerAndConnectionRule extends ExternalResource {

        @Override
        protected void after() {
            
        }

        @Override
        protected void before() throws Throwable {
            
        }
        
    }
}
