/**
 * Support for ActiveMQ-Artemis clients.
 *
 * <p>Provides for {@Inject}ion of {@code ConnectionFactory} instances.</p>
 *
 * <p>Configuration through properties:</p>
 *
 * <ul>
 *     <li>{@code artemis.host}: Broker host if not using URL below.</li>
 *     <li>{@code artemis.port}: Broker port if not using URL below.</li>
 *     <li>{@code artemis.url}: Broker url if not using host/port above.</li>
 *     <li>{@code artemis.username}: Broker username for authentication.</li>
 *     <li>{@code artemis.password}: Broker password for authentication.</li>
 * </ul>
 */
package io.thorntail.jms.artemis;