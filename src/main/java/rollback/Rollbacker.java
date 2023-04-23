package rollback;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import data.Block;
import order.Order;
import redis.clients.jedis.Jedis;
import redisTools.ReadRedis;
import servers.EsTools;
import startAPIP.IndicesAPIP;
import startAPIP.RedisKeys;
import writeEs.IndicesFCH;

import java.io.IOException;
import java.util.ArrayList;

public class Rollbacker {
    /**
     * 检查上一个orderHeight与orderBid是否一致
     * 不一致则orderHeight减去30
     * 对回滚区块的es的order做减值处理。
    * */
    public static boolean isRolledBack(ElasticsearchClient esClient,long lastHeight,String lastBlockId) throws IOException {
        if(esClient==null) {
            System.out.println("Failed to check rollback. Start a ES client first.");
            return false;
        }

        if (lastHeight==0 || lastBlockId ==null){
            System.out.println("Failed to check rollback. Bad block information.");
        }

        Block block = EsTools.getById(esClient, IndicesFCH.BlockIndex,lastBlockId , Block.class);

        if(block==null){
            return true;
        }else return false;
    }

    public static void rollback(ElasticsearchClient esClient, long height) throws Exception {

      ArrayList<Order> orderList = EsTools.getListSinceHeight(esClient, IndicesAPIP.OrderIndex,"height",height,Order.class);

      minusFromBalance(esClient,orderList);

      new Jedis().set(RedisKeys.OrderLastHeight, String.valueOf(height));
    }

    private static void minusFromBalance(ElasticsearchClient esClient, ArrayList<Order> orderList) throws Exception {
        ArrayList<String> idList= new ArrayList<>();
        Jedis jedis = new Jedis();
        for(Order order: orderList){
            String addr = order.getFromAddr();
            long balance = ReadRedis.readHashLong(jedis, RedisKeys.Balance, addr);
            jedis.hset(RedisKeys.Balance,addr, String.valueOf(balance-order.getAmount()));

            idList.add(order.getId());
        }
        EsTools.bulkDeleteList(esClient, IndicesAPIP.OrderIndex, idList);
    }
}
