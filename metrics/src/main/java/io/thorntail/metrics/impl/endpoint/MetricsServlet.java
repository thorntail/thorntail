package io.thorntail.metrics.impl.endpoint;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.thorntail.metrics.impl.exporters.Exporter;
import io.thorntail.metrics.impl.exporters.JSONExporter;
import io.thorntail.metrics.impl.exporters.JSONMetadataExporter;
import io.thorntail.metrics.impl.exporters.PrometheusExporter;
import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Created by bob on 1/22/18.
 */
public class MetricsServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Exporter exporter = getExporterForOPTIONS(request);
        if ( exporter == null ) {
            response.sendError(406);
            return;
        }
        service(exporter, request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Exporter exporter = getExporterForGET(request);
        service(exporter, request, response);
    }

    protected void service(Exporter exporter, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (exporter == null) {
            response.sendError(404);
            return;
        }

        response.setContentType(exporter.getContentType());
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        String path = request.getPathInfo();
        if (path == null) {
            export(exporter.exportAllScopes(), response);
            return;
        }

        String[] parts = partsOf(path);
        String scope = null;
        String metricName = null;
        if (parts.length >= 1) {
            scope = parts[0];
        }
        if (parts.length == 2) {
            metricName = parts[1];
        }

        MetricRegistry.Type registryType = null;
        try {
            registryType = MetricRegistry.Type.valueOf(scope.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.sendError(404);
            return;
        }

        if (metricName == null) {
            export(exporter.exportOneScope(registryType), response);
            return;
        }


        export(exporter.exportOneMetric(registryType, metricName), response);
    }

    private void export(StringBuffer data, HttpServletResponse response) throws IOException {
        try (Writer out = new OutputStreamWriter(response.getOutputStream())) {
            out.write(data.toString());
        }
    }

    String[] partsOf(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.split("/");
    }

    protected Exporter getExporterForGET(HttpServletRequest request) {
        if ("metadata".equalsIgnoreCase(request.getParameter("format"))) {
            return this.jsonMetadataExporter;
        }
        if (isJSON(request)) {
            return this.jsonExporter;
        }

        return this.prometheusExporter;
    }

    protected Exporter getExporterForOPTIONS(HttpServletRequest request) {
        if (isJSON(request)) {
            return this.jsonMetadataExporter;
        }

        return null;
    }

    protected boolean isJSON(HttpServletRequest request) {
        String format = request.getParameter("format");
        if (format != null && "json".equalsIgnoreCase(format)) {
            return true;
        }
        String acceptHeader = request.getHeader("accept");
        String[] types = acceptHeader.split(",");
        for (String type : types) {
            if (type.startsWith("application/json")) {
                return true;
            }
        }

        return false;
    }

    @Inject
    JSONExporter jsonExporter;

    @Inject
    JSONMetadataExporter jsonMetadataExporter;

    @Inject
    PrometheusExporter prometheusExporter;
}

