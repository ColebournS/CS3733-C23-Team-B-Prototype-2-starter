package edu.wpi.teamb.Database.DAO;

import edu.wpi.teamb.Database.*;
import edu.wpi.teamb.Entities.SessionGetter;
import edu.wpi.teamb.Pathfinding.Pathfinding;
import java.text.SimpleDateFormat;
import java.util.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class MapDAO {

  private static Map<String, Node> nodes = new HashMap<String, Node>();
  private static List<Edge> edges;
  private static Map<String, LocationName> locationNames = new HashMap<String, LocationName>();
  private static List<KioskMove> kioskMoves = new ArrayList<KioskMove>();
  private static Map<String, List<Move>> IDMoves = new HashMap<String, List<Move>>();

  public static Map<String, Node> getAllNodes() {
    refreshNodes();
    return nodes;
  }

  public static List<Edge> getAllEdges() {
    refreshEdges();
    return edges;
  }

  public static Map<String, LocationName> getAllLocationNames() {
    refreshLocationNames();
    return locationNames;
  }

  public static List<KioskMove> getAllKioskMoves() {
    refreshKioskMoves();
    return kioskMoves;
  }

  public static Map<String, List<Move>> getIDMoves() {
    return IDMoves;
  }

  public static void refreshIDMoves(Date d) {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    HashMap<String, List<Move>> moves = new HashMap<String, List<Move>>();
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String hql =
        "SELECT DISTINCT locationName, node, moveDate FROM Move WHERE moveDate <= '"
            + d
            + "' ORDER BY moveDate DESC";
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery(hql);
      List<Object[]> ms = q.list();
      tx.commit();
      for (Object[] moveInfo : ms) {
        Move m = new Move();
        m.setLocationName((LocationName) moveInfo[0]);
        m.setNode((Node) moveInfo[1]);
        m.setMoveDate((Date) moveInfo[2]);
        if (moves.containsKey(m.getNode().getNodeID())) {
          String d1 = fmt.format(m.getMoveDate());
          String d2 = fmt.format(moves.get(m.getNode().getNodeID()).get(0).getMoveDate());
          if (d1.equals(d2)) {
            moves.get(m.getNode().getNodeID()).add(m);
          } else {
            ArrayList<Move> newM = new ArrayList<Move>();
            newM.add(m);
            moves.put(m.getNode().getNodeID(), newM);
          }
        } else {
          ArrayList<Move> newM = new ArrayList<Move>();
          newM.add(m);
          moves.put(m.getNode().getNodeID(), newM);
        }
      }
      IDMoves = moves;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static Map<String, Move> getLNMoves(Date d) {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    HashMap<String, Move> moves = new HashMap<String, Move>();
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String hql =
        "SELECT DISTINCT locationName, node, moveDate FROM Move WHERE moveDate <= '"
            + d
            + "' ORDER BY moveDate DESC";
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery(hql);
      List<Object[]> ms = q.list();
      tx.commit();
      for (Object[] moveInfo : ms) {
        Move m = new Move();
        m.setLocationName((LocationName) moveInfo[0]);
        m.setNode((Node) moveInfo[1]);
        m.setMoveDate((Date) moveInfo[2]);
        if (moves.containsKey(m.getLocationName().getLongName())) {
          Date d1 = moves.get(m.getLocationName().getLongName()).getMoveDate();
          Date d2 = (Date) moveInfo[2];
          if (moves.get(m.getLocationName().getLongName()).getMoveDate().before((Date) moveInfo[2]))
            moves.put(m.getLocationName().getLongName(), m);
        } else moves.put(m.getLocationName().getLongName(), m);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      return moves;
    }
  }

  public static List<Move> getFutureMoves(Date d) {
    List<Move> moves = new ArrayList<Move>();
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String hql = "FROM Move WHERE moveDate >= '" + d + "'";
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery(hql, Move.class);
      moves = q.list();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      return moves;
    }
  }

  public static void refreshNodes() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Node", Node.class);
      List<Node> ns = q.list();
      tx.commit();
      nodes.clear();
      for (Node node : ns) nodes.put(node.getNodeID(), node);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void refreshEdges() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM Edge", Edge.class);
      edges = q.list();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void refreshLocationNames() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM LocationName", LocationName.class);
      List<LocationName> lns = q.list();
      tx.commit();
      locationNames.clear();
      for (LocationName ln : lns) locationNames.put(ln.getLongName(), ln);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void refreshKioskMoves() {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery("FROM KioskMove", KioskMove.class);
      kioskMoves = q.list();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void addNode(Node n) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.persist(n);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshNodes();
      refreshEdges();
    }
  }

  public static void deleteNode(Node n) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      session.remove(n);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshNodes();
      //      refreshEdges();
      Pathfinding.refreshData();
    }
  }

  public static void updateNode(Node n) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String oldID = n.getNodeID();
    String newID = n.buildID();
    String hql = "UPDATE Node n SET n.nodeID = '" + newID + "' WHERE n.nodeID = '" + oldID + "'";
    try {
      Transaction tx = session.beginTransaction();
      session.merge(n);
      session.createQuery(hql).executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshNodes();
      refreshEdges();
    }
  }

  public static void addEdge(Edge ed) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.persist(ed);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshEdges();
    }
  }

  public static void deleteEdge(Node n1, Node n2) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    Edge e1 = new Edge();
    Edge e2 = new Edge();
    e1.setNode1(n1);
    e1.setNode2(n2);
    e2.setNode1(n2);
    e2.setNode2(n1);
    try {
      Transaction tx = session.beginTransaction();
      session.remove(e1);
      session.remove(e2);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshEdges();
    }
  }

  public static void addLocationName(LocationName ln) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.persist(ln);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshLocationNames();
    }
  }

  public static void deleteLocationName(LocationName ln) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      session.remove(ln);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshLocationNames();
    }
  }

  public static void updateLocationName(LocationName newLN, LocationName oldLN) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String oldLNName = oldLN.getLongName();
    String hql =
        "UPDATE LocationName ln SET ln.longName = '"
            + newLN.getLongName()
            + "', ln.shortName = '"
            + newLN.getShortName()
            + "', ln.locationType = '"
            + newLN.getLocationType()
            + "' WHERE ln.longName = '"
            + oldLNName
            + "'";
    try {
      Transaction tx = session.beginTransaction();
      session.createQuery(hql, LocationName.class).executeUpdate();
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      refreshLocationNames();
    }
  }

  public static void addMove(Move m) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.persist(m);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void deleteMove(Move m) {
    KioskMove km = new KioskMove();
    km.setNextNode(m.getNode());
    km.setLocationName(m.getLocationName());
    km.setMoveDate(m.getMoveDate());
    deleteKioskMove(km);
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.remove(m);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void addKioskMove(KioskMove km) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.persist(km);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void deleteKioskMove(KioskMove km) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    try {
      Transaction tx = session.beginTransaction();
      session.remove(km);
      tx.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
    }
  }

  public static void updateMove(Move oldM, Move newM) {
    List<KioskMove> ks = getAllKioskMoves();
    KioskMove newK = new KioskMove();
    for (KioskMove k : ks) {
      if (k.getNextNode().getNodeID().equals(oldM.getNode().getNodeID())
          && k.getLocationName().getLongName().equals(oldM.getLocationName().getLongName())
          && k.getMoveDate().equals(oldM.getMoveDate())) {
        newK.setMessage(k.getMessage());
        newK.setPrevNode(k.getPrevNode());
        deleteKioskMove(k);
      }
    }
    newK.setNextNode(newM.getNode());
    newK.setMoveDate(newM.getMoveDate());
    newK.setLocationName(newM.getLocationName());
    deleteMove(oldM);
    addMove(newM);
    addKioskMove(newK);
  }

  public static Move getMostRecentMoveWithLocationName(LocationName ln) {
    SessionFactory sf = SessionGetter.CONNECTION.getSessionFactory();
    Session session = sf.openSession();
    String hql = "FROM Move WHERE locationName = '" + ln.getLongName() + "' ORDER BY moveDate DESC";
    Move m = new Move();
    try {
      Transaction tx = session.beginTransaction();
      Query q = session.createQuery(hql, Move.class);
      tx.commit();
      List<Move> ms = q.list();
      if (!ms.isEmpty()) {
        m = ms.get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.close();
      return m;
    }
  }
}
