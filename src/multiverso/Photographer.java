package multiverso;

import java.util.Scanner;

public class Photographer {

    public static void main(String[] args) {
        Tree tree = new Tree();
        tree.startInteraction();
    }
}

class Tree {
    
    private Scanner scanner = new Scanner(System.in);
    private Node root;
    private static final int LIMITE_SALTOS = 10; // Límite de saltos
    private int contadorSaltos = 0; // Contador de saltos

    public Tree() {
        root = null;
    }

    public void startInteraction() {
        
        initializeTree();

        Node currentNode = root;

        while (contadorSaltos < LIMITE_SALTOS) {
            System.out.println("Nodo actual: " + currentNode.dato);
            System.out.println("Elige un nodo hijo (1, 2, 3, 4):");

            if (currentNode.sonA != null) {
                System.out.println("1: " + currentNode.sonA.dato);
            }
            if (currentNode.sonB != null) {
                System.out.println("2: " + currentNode.sonB.dato);
            }
            if (currentNode.sonC != null) {
                System.out.println("3: " + currentNode.sonC.dato);
            }
            if (currentNode.sonD != null) {
                System.out.println("4: " + currentNode.sonD.dato);
            }

            int choice = Integer.parseInt(scanner.nextLine());
            Node selectedNode = null;

            switch (choice) {
                case 1 -> selectedNode = currentNode.sonA;
                case 2 -> selectedNode = currentNode.sonB;
                case 3 -> selectedNode = currentNode.sonC;
                case 4 -> selectedNode = currentNode.sonD;
                default -> {
                    System.out.println("Opción no válida. Intenta de nuevo.");
                    continue;
                }
            }

            if (selectedNode != null) {
                keepOnlyPathToNode(selectedNode);
                currentNode = selectedNode;

                if (contadorSaltos < LIMITE_SALTOS - 1) { 
                    createChildren(currentNode);
                }

                contadorSaltos++; // Incrementar el contador de saltos
                System.out.println("Salto realizado. Saltos restantes: " + 
                        (LIMITE_SALTOS - contadorSaltos));
            } else {
                System.out.println("Nodo no válido. Intenta de nuevo.");
            }
        }

        System.out.println("Se acabaron los saltos.");
        scanner.close();
    }

    private void initializeTree() {
        root = new Node(0); 
        createChildren(root);
    }

    private void createChildren(Node parent) {
        parent.sonA = new Node(parent.dato * 10 + 1); 
        parent.sonB = new Node(parent.dato * 10 + 2); 
        parent.sonC = new Node(parent.dato * 10 + 3); 
        parent.sonD = new Node(parent.dato * 10 + 4); 
    }

    private void keepOnlyPathToNode(Node selectedNode) {
        if (selectedNode == null) {
            return;
        }

        Node parent = findParent(root, selectedNode);
        if (parent != null) {
            if (parent.sonA != selectedNode) {
                parent.sonA = null;
            }
            if (parent.sonB != selectedNode) {
                parent.sonB = null;
            }
            if (parent.sonC != selectedNode) {
                parent.sonC = null;
            }
            if (parent.sonD != selectedNode) {
                parent.sonD = null;
            }
        }
    }

    private Node findParent(Node current, Node target) {
        if (current == null || current == target) {
            return null;
        }

        if (current.sonA == target || current.sonB == target || current.sonC 
                == target || current.sonD == target) {
            return current;
        }

        Node result = findParent(current.sonA, target);
        if (result != null) {
            return result;
        }

        result = findParent(current.sonB, target);
        if (result != null) {
            return result;
        }

        result = findParent(current.sonC, target);
        if (result != null) {
            return result;
        }

        return findParent(current.sonD, target);
    }

    private class Node {

        int dato;
        Node sonA, sonB, sonC, sonD;

        private Node(int dato) {
            this.dato = dato;
            this.sonA = this.sonB = this.sonC = this.sonD = null;
        }
    }
}

//visual




