# **CodeTori: Function UML Visualizer for IntelliJ IDEA**

CodeTori is an IntelliJ plugin that makes understanding and reviewing Java functions easier. It generates UML diagrams for any function in a Java file, enabling both developers and non-developers to quickly grasp the purpose and flow of the code.

---

## **Features**
- Generate UML diagrams for functions at the click of a button.
- Visualize code logic in an easy-to-understand format.
- Help non-developers (e.g., project managers) review code implementations for business alignment.

---

## **Getting Started**

### **Prerequisites**
- IntelliJ IDEA 2022.2 or later
- JDK 17+
- Gradle installed

---

### **Running CodeTori Locally**

1. **Clone the Repository**  
   ```bash
   git clone https://github.com/olufemithompson/code-tori.git
2. Open in IntelliJ IDEA
   - Open IntelliJ IDEA.
   - Navigate to File > Open and select the `code-tori/idea-plugin/` project directory.
3. Set Up Gradle
   - Ensure Gradle is set up to use the `intellij` plugin.
   - IntelliJ will automatically download dependencies based on the `build.gradle` file.
4. Run the Plugin Locally
   - Open the Gradle tool window in IntelliJ.
   - Go to Tasks > intellij and double-click `runIde`.
   - A new IntelliJ IDEA instance will launch with CodeTori installed for testing.
  
---

### **Using the Plugin**

1. Open a Java file in the new instance of IntelliJ.
2. Place your cursor on any function in the code.
3. Click the `Explain This Function` menu item or use the right-click context menu to generate a UML diagram.
4. A popup will appear with the UML diagram for the selected function.
