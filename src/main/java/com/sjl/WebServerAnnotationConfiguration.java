package com.sjl;

import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebServerAnnotationConfiguration extends AnnotationConfiguration {
    @Override
    public void configure(WebAppContext context) throws Exception {
        boolean metadataComplete = context.getMetaData().isMetaDataComplete();
        context.addDecorator(new AnnotationDecorator(context));


//Even if metadata is complete, we still need to scan for ServletContainerInitializers - if there are any
        AnnotationParser parser = null;
        if (!metadataComplete) {
//If metadata isn't complete, if this is a servlet 3 webapp or isConfigDiscovered is true, we need to search for annotations
            if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered()) {
                _discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context));
                _discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context));
                _discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context));
            }
        }

//Regardless of metadata, if there are any ServletContainerInitializers with @HandlesTypes, then we need to scan all the
//classes so we can call their onStartup() methods correctly
        createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context));

        if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty()) {
            parser = new AnnotationParser() {
                @Override
                public void parse(Resource aDir, ClassNameResolver aResolver) throws Exception {
                    if (aDir.isDirectory())
                        super.parse(aDir, aResolver);
                    else
                        super.parse(aDir.getURI(), aResolver);
                }
            };

            parse(context, parser);

            for (AnnotationParser.DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers)
                context.getMetaData().addDiscoveredAnnotations(((AbstractDiscoverableAnnotationHandler) h).getAnnotationList());
        }

    }

    private void parse(final WebAppContext context, AnnotationParser parser) throws Exception {
        List<Resource> _resources = getResources(getClass().getClassLoader());
        for (Resource _resource : _resources) {
            if (_resource == null)
                return;

            parser.clearHandlers();
            for (AnnotationParser.DiscoverableAnnotationHandler h : _discoverableAnnotationHandlers) {
                if (h instanceof AbstractDiscoverableAnnotationHandler)
                    ((AbstractDiscoverableAnnotationHandler) h).setResource(null); //
            }
            parser.registerHandlers(_discoverableAnnotationHandlers);
            parser.registerHandler(_classInheritanceHandler);
            parser.registerHandlers(_containerInitializerAnnotationHandlers);

            parser.parse(_resource, new ClassNameResolver() {
                public boolean isExcluded(String name) {
                    if (context.isSystemClass(name)) return true;
                    if (context.isServerClass(name)) return false;
                    return false;
                }

                public boolean shouldOverride(String name) {
//looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                    if (context.isParentLoaderPriority())
                        return false;
                    return true;
                }
            });
        }
    }

    private List<Resource> getResources(ClassLoader aLoader) throws IOException {
        if (aLoader instanceof URLClassLoader) {
            List<Resource> _result = new ArrayList<Resource>();
            URL[] _urls = ((URLClassLoader) aLoader).getURLs();
            for (URL _url : _urls)
                _result.add(Resource.newResource(_url));

            return _result;
        }
        return Collections.emptyList();
    }
}
