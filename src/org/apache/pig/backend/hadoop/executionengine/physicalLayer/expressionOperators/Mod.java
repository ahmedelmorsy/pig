/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pig.backend.hadoop.executionengine.physicalLayer.expressionOperators;

import java.math.BigInteger;

import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.POStatus;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.Result;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.plans.PhyPlanVisitor;
import org.apache.pig.data.DataType;
import org.apache.pig.impl.plan.NodeIdGenerator;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.impl.plan.VisitorException;

public class Mod extends BinaryExpressionOperator {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Mod(OperatorKey k) {
        super(k);
    }

    public Mod(OperatorKey k, int rp) {
        super(k, rp);
    }

    @Override
    public void visit(PhyPlanVisitor v) throws VisitorException {
        v.visitMod(this);
    }

    @Override
    public String name() {
        return "Mod" + "[" + DataType.findTypeName(resultType) + "]" +" - " + mKey.toString();
    }

    protected Number mod(Number a, Number b, byte dataType) throws ExecException {
        switch(dataType) {
        case DataType.INTEGER:
            return Integer.valueOf((Integer) a % (Integer) b);
        case DataType.LONG:
            return Long.valueOf((Long) a % (Long) b);
        case DataType.BIGINTEGER:
            return ((BigInteger)a).mod((BigInteger)b);
        default:
            throw new ExecException("called on unsupported Number class " + DataType.findTypeName(dataType));
        }
    }

    protected Result genericGetNext(byte dataType) throws ExecException {
        Result r = accumChild(null, dataType);
        if (r != null) {
            return r;
        }

        byte status;
        Result res;
        res = lhs.getNext(dataType);
        status = res.returnStatus;
        if(status != POStatus.STATUS_OK || res.result == null) {
            return res;
        }
        Number left = (Number) res.result;

        res = rhs.getNext(dataType);
        status = res.returnStatus;
        if(status != POStatus.STATUS_OK || res.result == null) {
            return res;
        }
        Number right = (Number) res.result;

        res.result = mod(left, right, dataType);
        return res;
    }

    @Override
    public Result getNextInteger() throws ExecException{
        return genericGetNext(DataType.INTEGER);
    }

    @Override
    public Result getNextLong() throws ExecException{
        return genericGetNext(DataType.LONG);
    }

    @Override
    public Result getNextBigInteger() throws ExecException{
        return genericGetNext(DataType.BIGINTEGER);
    }

    @Override
    public Mod clone() throws CloneNotSupportedException {
        Mod clone = new Mod(new OperatorKey(mKey.scope,
            NodeIdGenerator.getGenerator().getNextNodeId(mKey.scope)));
        clone.cloneHelper(this);
        return clone;
    }

}
