package edu.wpi.teamb.Database;

import edu.wpi.teamb.Entities.*;
import edu.wpi.teamb.SessionGetter;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class DBSession {

  private static DBSession instance = null;

  private DBSession() {};

  public static DBSession getInstance() {
    if (instance == null) {
      instance = new DBSession();
    }
    return instance;
  }

  public static void addORM(Object o) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      session.persist(o);
      tx.commit();
    } catch (Exception e) {
      if (tx != null) tx.rollback();
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static List<Object> getAll(ORMType ot) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM " + ot.toString());
      List<Object> objects = q.list();
      session.close();
      return objects;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static List<Edge> getAllEdges() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Edge");
      List<Edge> edges = q.list();
      session.close();
      return edges;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static List<Move> getAllMoves() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Move");
      List<Move> moves = q.list();
      session.close();
      return moves;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static List<Move> getAllMovesWithLN(String ln) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Move WHERE longName = '" + ln + "'");
      List<Move> moves = q.list();
      session.close();
      return moves;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static List<Node> getAllNodes() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Node");
      List<Node> nodes = q.list();
      session.close();
      return nodes;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static List<LocationName> getAllLN() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM LocationName");
      List<LocationName> lns = q.list();
      session.close();
      return lns;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  public static void delete(IORM iorm) {

    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      String str = "DELETE " + iorm.getSearchStr();
      session.createQuery(str).executeUpdate();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void updateLocationName(LocationName newLN, LocationName oldLN) {
    if (newLN.getLongName().equals(oldLN.getLongName())) {
      delete(oldLN);
      addORM(newLN);
    } else {
      List<Move> moves = getAllMoves();
      for (Move m : moves) {
        if (m.getLongName().equals(oldLN.getLongName())) {
          Move newm = new Move(m.getNodeID(), newLN.getLongName(), m.getMoveDate());
          addORM(newm);
          delete(m);
        }
      }
    }
  }

  public static void updateNode(Node n) {

    Node ncopy = new Node();
    ncopy.setNodeID(n.buildID());
    ncopy.setXcoord(n.getXcoord());
    ncopy.setYcoord(n.getYcoord());
    ncopy.setFloor(n.getFloor());
    ncopy.setBuilding(n.getBuilding());

    List<Edge> edges = getAllEdges();
    for (Edge e : edges) {
      if (e.getNode1().equals(n.getNodeID())) {
        Edge newe = new Edge();
        newe.setNode1(ncopy.getNodeID());
        newe.setNode2(e.getNode2());
        addORM(newe);
        delete(e);
      } else if (e.getNode2().equals(n.getNodeID())) {
        Edge newe = new Edge();
        newe.setNode1(e.getNode1());
        newe.setNode2(ncopy.getNodeID());
        addORM(newe);
        delete(e);
      }
    }

    List<Move> moves = getAllMoves();
    for (Move m : moves) {
      if (m.getNodeID().equals(n.getNodeID())) {
        Move newm = new Move();
        newm.setNodeID(ncopy.getNodeID());
        newm.setLongName(m.getLongName());
        newm.setMoveDate(m.getMoveDate());
        addORM(newm);
        delete(m);
      }
    }

    addORM(ncopy);
    delete(n);
  }

  public static void updateAllNodes() {

    List<Node> nodes = getAllNodes();

    for (Node n : nodes) {
      updateNode(n);
    }
  }

   public static String getMostRecentLocation(String NodeID) {
      return getMostRecentMove(NodeID).getLongName();
    }

   public static String getMostRecentNode(String longName) {
      List<Move> moves = getAllMovesWithLN(longName);

      if (moves == null) return "NO MOVES";

      Move mostRecent = moves.get(0);
      for (Move move : moves) if (moreRecentThan(move, mostRecent)) mostRecent = move;

      return mostRecent.getNodeID();
    }

  public static Move getMostRecentMove(String nodeID) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    List<Move> moves = null;
    try {
      Transaction tx = session.beginTransaction();
      String str = "FROM Move WHERE nodeID = '" + nodeID + "'";
      Query q = session.createQuery(str);
      moves = q.list();
      session.close();
      if(moves.isEmpty()) {
        return null;
      } else {
        Move mostRecent = moves.get(0);
            for (Move move : moves) if (moreRecentThan(move, mostRecent)) mostRecent = move;
            return mostRecent;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      session.close();
    }
  }

  private static boolean moreRecentThan(Move move1, Move move2) {
    return move1.getMoveDate().after(move2.getMoveDate());
  }
}
