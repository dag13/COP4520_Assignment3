// Deron Gentles
// Programming Assignment #3
// Problem 1: The Birthday Presents Party (50 points)
// COP4520 Spring 2022

import java.util.*;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;

public class BirthdayPresentsParty {
	static final int NUM_SERVANTS = 4;
	static final int NUM_PRESENTS = 500000;

  public static void main(String[] args) {
		LockFreeList chain = new LockFreeList();
		Thread[] servants = new Thread[NUM_SERVANTS];

		ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < NUM_PRESENTS; i++)
			list.add(i);

		Collections.shuffle(list);

		for (int i = 0; i < NUM_SERVANTS; i++) {
			servants[i] = new Thread(new Present(chain, list, i));
			servants[i].start();
		}
  }
}


class Present implements Runnable {
	static final int NUM_PRESENTS = 500000;
	static LockFreeList chain;
	static 	ArrayList<Integer> bag;
	static AtomicInteger thankYouNotes;
	static Random rand = new Random();
	boolean addFlag;
	int tagNum;

	public Present(LockFreeList chain, ArrayList<Integer> list, int num) {
		this.chain = chain;
		bag = list;
		tagNum = num;
		addFlag = true;
		thankYouNotes = new AtomicInteger(0);
	}

	// This run method handles all thre of the servant actions until all notes are written
	// The threads continue to work until chain is empty and the amount of thank you notes is equal
	// to the total amount of presents recieved.
	// There is a 5% chance that the Minotaur gets impatient and asks to check
	// whether or not a particular gift is in the chain
	// In my implementation, a server takes a present from the shuffled ArrayList which represents an
	// unordered bag of presents and adds it to the chain. The addFlag is set to false so their next action
	// will be to remove a present from the chain.
	@Override
	public void run() {
		while(true) {
			if (chain.isEmpty())
				if (thankYouNotes.intValue() >= NUM_PRESENTS)
					return;

			int num = rand.nextInt(20);
			if (num == 0) {
				int tag = rand.nextInt(NUM_PRESENTS);
				chain.contains(tag);
				continue;
			}

			if (addFlag) {
				tagNum = bag.get(thankYouNotes.intValue());
				chain.add(tagNum);
				//System.out.println(tagNum);
				addFlag = false;
				continue;
			}

			else {
				chain.remove(tagNum);
				//System.out.println(tagNum);
				thankYouNotes.getAndIncrement();

				if (thankYouNotes.intValue() < NUM_PRESENTS)
					addFlag = true;
			}
		}
	}
}

// The following code for a Non-Blocking Linked List is from Chapter 9 of the textbook
// The Art of Multiprocessor Programming.Figures: 9.24, 9.25, 9.26, 9.27
class LockFreeList {
    final Node head;
		public volatile AtomicInteger size;

    public LockFreeList() {
        head = new Node(Integer.MIN_VALUE);
				Node sentinal = new Node(Integer.MAX_VALUE);
        Node tail = new Node(Integer.MAX_VALUE);
        head.next = new AtomicMarkableReference<Node>(tail, false);
        tail.next = new AtomicMarkableReference<Node>(sentinal, false);
				size = new AtomicInteger(0);
    }

    public boolean add( int tag) {
        while(true) {
            Window window = Window.find(head, tag);
            Node pred = window.pred, curr = window.curr;
            if(curr.tag == tag)
                return false;
            else {
                Node node = new Node(tag);
                node.next = new AtomicMarkableReference<>(curr, false);
                if(pred.next.compareAndSet(curr, node, false, false)) {
									size.getAndIncrement();
									return true;
                }
            }
        }
    }

		public boolean isEmpty() {
			return (getSize() == 0 ? true : false);
		}

		public int getSize() {
			return size.intValue();
		}

    public boolean remove( int tag) {
        boolean snip;
        while(true) {
            Window window = Window.find(head, tag);
            Node pred = window.pred, curr = window.curr;
            if(curr.tag != tag)
                return false;
            else {
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if(!snip)
                    continue;

                pred.next.compareAndSet(curr, succ, false, false);
								size.getAndDecrement();
                return true;
            }
        }
    }

    public boolean contains(int tag) {
        boolean[] marked = {false};
        Node curr = head;
        while(curr.tag < tag) {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }
        return (curr.tag == tag && !marked[0]);
    }
}

class Node {
    int tag;
    public volatile AtomicMarkableReference<Node> next;
    public Node(int tag) {
        this.tag = tag;
    }
}

class Window {
    public Node pred, curr;

    Window(Node myPred, Node myCurr) {
        pred = myPred; curr = myCurr;
    }

    public static Window find(Node head, int tag) {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry: while(true) {
            pred = head;
            curr = pred.next.getReference();
            while(true) {
                pred = head;
                curr = pred.next.getReference();
                while(true) {
                    succ = curr.next.get(marked);
                    while(marked[0]) {
                        snip =  pred.next.compareAndSet(curr, succ, false, false);
                        if(!snip) continue retry;
                        curr = succ;
                        succ = curr.next.get(marked);
                    }
                    if(curr.tag >= tag)
                        return new Window(pred, curr);

                    pred = curr;
                    curr = succ;
                }
            }
        }
    }
}
