package camelinaction;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCopierWithCamel
{

    public static void main( String args[] )
        throws
        Exception
    {
        final Logger LOGGER = LoggerFactory.getLogger( FileCopierWithCamel.class );

        CamelContext context = new DefaultCamelContext();

        context.addRoutes( new RouteBuilder()
        {
            public void configure()
            {
                from( "file:data/inbox?noop=true" )
                    .routeId( "Copy File From Inbox" )
                    .log( LoggingLevel.INFO, LOGGER, "Copying from inbox..." )
                    .to( "file:data/outbox" )
                    .log( LoggingLevel.INFO, LOGGER, "Added to outbox..." );
            }
        } );

        context.start();
        Thread.sleep( 5000 );
        context.addRoutes( new RouteBuilder()
        {
            @Override
            public void configure()
                throws
                Exception
            {
                from( "file:data/outbox?noop=true" )
                    .log( LoggingLevel.INFO, LOGGER, "Hello World" )
                    .setBody( simple( "This is a new body. The previous body was ${body}" ) )
                    .to( "file:data/newBody" );
            }
        } );
        context.addRoutes( new RouteBuilder()
        {
            @Override
            public void configure()
                throws
                Exception
            {
                from( "timer:mockDataTimer?period=1000" )
                    .routeId( "Generate Mock Data Route Builder" )
                    .setBody().constant( "Hello world" )
                    .log( LoggingLevel.INFO, LOGGER, "Generating mock data file" )
                    .to( "file:data/mockFiles?fileName=mockData_${date:now:yyyyMMddHHmmss}.txt" );
            }
        } );
        context.start();
        context.suspendRoute( "Generate Mock Data Route Builder" );
        Thread.sleep( 5000 );
        context.suspend();
        Thread.sleep( 5000 );
        // should be no new mock files in the inbox...
        context.resume();
        context.resumeRoute( "Generate Mock Data Route Builder" );
        Thread.sleep( 3000 );

        context.stop();
    }

}
