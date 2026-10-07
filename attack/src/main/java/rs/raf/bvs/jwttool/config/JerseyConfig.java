package rs.raf.bvs.jwttool.config;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        packages("rs.raf.bvs.jwttool.resources",
                 "rs.raf.bvs.jwttool.config");
    }
}
