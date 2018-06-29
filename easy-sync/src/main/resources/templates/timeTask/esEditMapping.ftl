<#ftl encoding="utf-8">


<div class="panel panel-default">
    <div class="panel-heading">
         <h3 class="panel-title"  lang-key="fieldMapping">Field mapping</h3>


    </div>

    <div class="panel-body">

        <div class="row">
            <div class="col-md-12">
                <table class="table table-condensed">
                    <tr>
                        <th lang-key="sourceFieldName">source field</th>
                        <th lang-key="sourceType">source type</th>
                        <th lang-key="targetFieldName">field</th>
                        <th lang-key="targetType">type</th>
                        <th lang-key="convert">convert</th>
                        <th lang-key="custom">custom</th>
                        <th lang-key="operation">operation</th>
                    </tr>

                    <tr v-for="(field,index) in esMapping.esFields">
                        <td><input type="text" class="form-control"  v-model="field.source"></td>
                        <td><input type="text" class="form-control"  v-model="field.sourceType"></td>
                        <td><input type="text" class="form-control"  v-model="field.target"></td>
                        <td><select v-model="field.type">

                            <option value="keyword" selected>keyword(version >=5.x)</option>
                            <option value="text">text(version >=5.x)</option>
                            <option value="string">string(version <5.x)</option>
                            <option value="integer">integer</option>
                            <option value="long">long</option>
                            <option value="short">short</option>
                            <option value="byte">byte</option>
                            <option value="double">double</option>
                            <option value="float">float</option>
                            <option value="date">date</option>
                            <option value="boolean">boolean</option>


                        </select></td>
                        <td></td>
                        <td><input type="text" class="form-control"  v-model="field.custom"></td>
                        <td>
                            <button class="btn btn-xs btn-danger" @click="deleteField(index)" lang-key="delete">delete
                            </button>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="7">
                            <button class="btn btn-primary btn-xs " @click="insertField" lang-key="addField">addField</button>
                            <button class="btn btn-primary btn-xs " style="margin-left: 10px" @click="autoMapping" lang-key="autoMapping">autoMapping</button>
                            <button class="btn btn-danger btn-xs " style="float: right" @click="deleteAll" lang-key="deleteAll">deleteAll</button>
                        </td>

                    </tr>
                </table>

            </div>
        </div>


    </div>

</div>




