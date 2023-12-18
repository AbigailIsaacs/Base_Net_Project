
In this project I implemented a **Bayesian network** which allow you to model and analyze complex systems by representing probabilistic relationships among variables. They provide a powerful framework for reasoning under uncertainty, enabling tasks such as probabilistic inference, decision support, risk assessment, and causal analysis across diverse fields such as healthcare, finance, engineering, and artificial intelligence.
Bayesian Networks,  are a type of probabilistic graphical model that represents a set of random variables and their conditional dependencies via a directed acyclic graph (DAG). 

Here are the key components and concepts associated with Bayesian Networks:

**Directed Acyclic Graph (DAG)**:
The graphical structure of a Bayesian Network is represented by a directed acyclic graph, where nodes represent random variables, and directed edges represent probabilistic dependencies between the variables.
The absence of a direct edge between two nodes indicates conditional independence.

**Nodes:**
Each node in the graph corresponds to a random variable. These variables can be discrete or continuous and represent aspects of the modeled system.

**Edges:**
Directed edges between nodes indicate direct probabilistic dependencies. If there is an edge from node A to node B, it means that B is conditionally dependent on A.

**Conditional Probability Tables (CPTs):**
Each node in a Bayesian Network has an associated conditional probability table (CPT). The CPT specifies the conditional probability distribution of the node given its parents in the graph.
For example, for a node A with parents B and C, the CPT for A might specify P(A | B, C).

**Probabilistic Inference:**
Bayesian Networks provide a framework for reasoning under uncertainty. Given observed evidence (values of some variables), they allow for the computation of probabilities of other variables in the network.
Inference involves updating probabilities based on observed evidence using Bayes' theorem.
