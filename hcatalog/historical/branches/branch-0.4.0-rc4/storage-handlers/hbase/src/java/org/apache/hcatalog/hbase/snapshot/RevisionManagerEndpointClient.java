package org.apache.hcatalog.hbase.snapshot;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * This class is nothing but a delegate for the enclosed proxy,
 * which is created upon setting the configuration.
 */
public class RevisionManagerEndpointClient implements RevisionManager, Configurable {

  private Configuration conf = null;
  private RevisionManager rmProxy;

  @Override
  public Configuration getConf() {
    return this.conf;
  }

  @Override
  public void setConf(Configuration arg0) {
    this.conf = arg0;
  }

  @Override
  public void initialize(Properties properties) {
    // do nothing
  }

  @Override
  public void open() throws IOException {
    // clone to adjust RPC settings unique to proxy
    Configuration clonedConf = new Configuration(conf);
    // conf.set("hbase.ipc.client.connect.max.retries", "0");
    // conf.setInt(HConstants.HBASE_CLIENT_RPC_MAXATTEMPTS, 1);
    clonedConf.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, 1); // do not retry RPC
    HTable table = new HTable(clonedConf, HConstants.META_TABLE_NAME);
    rmProxy = table.coprocessorProxy(RevisionManagerProtocol.class,
        Bytes.toBytes("anyRow"));
    rmProxy.open();
  }

  @Override
  public void close() throws IOException {
    rmProxy.close();
  }

  @Override
  public void createTable(String table, List<String> columnFamilies) throws IOException {
    rmProxy.createTable(table, columnFamilies);
  }

  @Override
  public void dropTable(String table) throws IOException {
    rmProxy.dropTable(table);
  }

  @Override
  public Transaction beginWriteTransaction(String table, List<String> families) throws IOException {
    return rmProxy.beginWriteTransaction(table, families);
  }

  @Override
  public Transaction beginWriteTransaction(String table, List<String> families, long keepAlive)
      throws IOException {
    return rmProxy.beginWriteTransaction(table, families, keepAlive);
  }

  @Override
  public void commitWriteTransaction(Transaction transaction) throws IOException {
    rmProxy.commitWriteTransaction(transaction);
  }

  @Override
  public void abortWriteTransaction(Transaction transaction) throws IOException {
    rmProxy.abortWriteTransaction(transaction);
  }

  @Override
  public List<FamilyRevision> getAbortedWriteTransactions(String table, String columnFamily)
      throws IOException {
    return rmProxy.getAbortedWriteTransactions(table, columnFamily);
  }

  @Override
  public TableSnapshot createSnapshot(String tableName) throws IOException {
    return rmProxy.createSnapshot(tableName);
  }

  @Override
  public TableSnapshot createSnapshot(String tableName, long revision) throws IOException {
    return rmProxy.createSnapshot(tableName, revision);
  }

  @Override
  public void keepAlive(Transaction transaction) throws IOException {
    rmProxy.keepAlive(transaction);
  }

}
