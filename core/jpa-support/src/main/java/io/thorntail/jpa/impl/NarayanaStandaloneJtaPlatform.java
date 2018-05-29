package io.thorntail.jpa.impl;


import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatformException;

/**
 * Configure Hibernate to use the Narayana TransactionManager in standalone mode.
 *
 * @author Sanne Grinovero (C) 2018 Red Hat Inc.
 */
final class NarayanaStandaloneJtaPlatform extends AbstractJtaPlatform {

   public NarayanaStandaloneJtaPlatform() {
   }

   @Override
   protected TransactionManager locateTransactionManager() {
      try {
         return com.arjuna.ats.jta.TransactionManager.transactionManager();
      }
      catch (Exception e) {
         throw new JtaPlatformException( "Could not obtain JBoss Transactions transaction manager instance", e );
      }
   }

   @Override
   protected UserTransaction locateUserTransaction() {
      try {
         return com.arjuna.ats.jta.UserTransaction.userTransaction();
      }
      catch (Exception e) {
         throw new JtaPlatformException( "Could not obtain JBoss Transactions user transaction instance", e );
      }
   }

}
