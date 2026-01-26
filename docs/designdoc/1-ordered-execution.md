# 1. Ordered execution

## Introduction
As of now, the execution of the tasks is unordered. This document describes the design of the ordered execution.
Ordered execution might be useful in some cases where the tasks are dependent on each other.

1. When the client decides the ordering, the client should get an identifier of the previous task, this to form a directed acyclic graph (DAG). A representation of this DAG should be sent to fup-reporter along with the new message.
2. When the client does not decide the ordering, the client must send its unique ordering identification along with the new message. The fup-reporter should form a DAG.

3. After successfully forming the DAG, the fup-reporter should execute the tasks in the order they are received.

### 1.1. Client generated DAG
#### Design

### 1.2. Server generated DAG
#### Design

### 1.3 Execution
#### Design
