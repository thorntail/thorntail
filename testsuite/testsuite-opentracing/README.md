# Thorntail - Test suite - OpenTracing

This module has integration tests for the OpenTracing fraction. It serves
also as an example of how a project can be bootstrapped with the OpenTracing
fraction and how to create "business spans", which are children of the span
started via the Servlet framework integration.

The image below is a visual representation of the spans, as if they were reported
to Jaeger. It contains a parent span that is automatically created by the Servlet
framework integration, plus the business span under that.

![Thorntail: Rightsize your JavaEE Applications](example.png)
