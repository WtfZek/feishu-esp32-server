<template>
    <div class="welcome">
        <HeaderBar />

        <div class="operation-bar">
            <h2 class="page-title">字典管理</h2>
            <div class="action-group">
                <div class="search-group">
                    <el-input placeholder="请输入字典值标签查询" v-model="search" class="search-input" clearable
                        @keyup.enter.native="handleSearch" style="width: 240px" />
                    <el-button class="btn-search" @click="handleSearch">
                        搜索
                    </el-button>
                </div>
            </div>
        </div>

        <!-- 主体内容 -->
        <div class="main-wrapper">
            <div class="content-panel">
                <!-- 左侧字典类型列表 -->
                <div class="dict-type-panel">
                    <div class="dict-type-header">
                        <el-button type="success" size="mini" @click="showAddDictTypeDialog">新增字典类型</el-button>
                        <el-button type="danger" size="mini" @click="batchDeleteDictType"
                            :disabled="selectedDictTypes.length === 0">
                            批量删除字典类型
                        </el-button>
                    </div>
                    <el-table ref="dictTypeTable" :data="dictTypeList" style="width: 100%" v-loading="dictTypeLoading"
                        element-loading-text="拼命加载中" element-loading-spinner="el-icon-loading"
                        element-loading-background="rgba(255, 255, 255, 0.7)" @row-click="handleDictTypeRowClick"
                        @selection-change="handleDictTypeSelectionChange" :row-class-name="tableRowClassName"
                        class="dict-type-table">
                        <el-table-column type="selection" width="55" align="center"></el-table-column>
                        <el-table-column label="字典类型名称" prop="dictName" align="center"></el-table-column>
                        <el-table-column label="操作" width="100" align="center">
                            <template slot-scope="scope">
                                <el-button type="text" size="mini" @click.stop="editDictType(scope.row)">编辑</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <!-- 右侧字典数据列表 -->
                <div class="content-area">
                    <el-card class="dict-data-card" shadow="never">
                        <el-table ref="dictDataTable" :data="dictDataList" style="width: 100%"
                            v-loading="dictDataLoading" element-loading-text="拼命加载中"
                            element-loading-spinner="el-icon-loading"
                            element-loading-background="rgba(255, 255, 255, 0.7)" class="data-table"
                            header-row-class-name="table-header">
                            <el-table-column label="选择" align="center" width="55">
                                <template slot-scope="scope">
                                    <el-checkbox v-model="scope.row.selected"></el-checkbox>
                                </template>
                            </el-table-column>
                            <el-table-column label="字典标签" prop="dictLabel" align="center"></el-table-column>
                            <el-table-column label="字典值" prop="dictValue" align="center"></el-table-column>
                            <el-table-column label="排序" prop="sort" align="center"></el-table-column>
                            <el-table-column label="操作" align="center" width="180px">
                                <template slot-scope="scope">
                                    <el-button type="text" size="mini" @click="editDictData(scope.row)"
                                        class="edit-btn">
                                        修改
                                    </el-button>
                                    <el-button type="text" size="mini" @click="deleteDictData(scope.row)"
                                        class="delete-btn">
                                        删除
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="table-footer">
                            <div class="batch-actions">
                                <el-button size="mini" type="primary" @click="selectAllDictData">
                                    {{ isAllDictDataSelected ? '取消全选' : '全选' }}
                                </el-button>
                                <el-button type="success" size="mini" @click="showAddDictDataDialog" class="add-btn">
                                    新增字典数据
                                </el-button>
                                <el-button size="mini" type="danger" icon="el-icon-delete" @click="batchDeleteDictData">
                                    批量删除字典数据
                                </el-button>
                            </div>
                            <div class="custom-pagination">
                                <el-select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
                                    <el-option v-for="item in pageSizeOptions" :key="item" :label="`${item}条/页`"
                                        :value="item">
                                    </el-option>
                                </el-select>

                                <button class="pagination-btn" :disabled="currentPage === 1" @click="goFirst">
                                    首页
                                </button>
                                <button class="pagination-btn" :disabled="currentPage === 1" @click="goPrev">
                                    上一页
                                </button>
                                <button v-for="page in visiblePages" :key="page" class="pagination-btn"
                                    :class="{ active: page === currentPage }" @click="goToPage(page)">
                                    {{ page }}
                                </button>
                                <button class="pagination-btn" :disabled="currentPage === pageCount" @click="goNext">
                                    下一页
                                </button>
                                <span class="total-text">共{{ total }}条记录</span>
                            </div>
                        </div>
                    </el-card>
                </div>
            </div>
        </div>

        <!-- 使用字典类型编辑弹框组件 -->
        <DictTypeDialog :visible.sync="dictTypeDialogVisible" :title="dictTypeDialogTitle" :dictTypeData="dictTypeForm"
            @save="saveDictType" />

        <!-- 使用字典数据编辑弹框组件 -->
        <DictDataDialog :visible.sync="dictDataDialogVisible" :title="dictDataDialogTitle" :dictData="dictDataForm"
            :dictTypeId="selectedDictType?.id" @save="saveDictData" />
        <el-footer style="flex-shrink:unset;">
            <version-footer />
        </el-footer>
    </div>
</template>

<script>
import dictApi from '@/apis/module/dict'
import DictDataDialog from '@/components/DictDataDialog.vue'
import DictTypeDialog from '@/components/DictTypeDialog.vue'
import HeaderBar from '@/components/HeaderBar.vue'
import VersionFooter from '@/components/VersionFooter.vue'
export default {
    name: 'DictManagement',
    components: {
        HeaderBar,
        DictTypeDialog,
        DictDataDialog,
        VersionFooter
    },
    data() {
        return {
            // 字典类型相关
            dictTypeList: [],
            dictTypeLoading: false,
            selectedDictType: null,
            selectedDictTypes: [],  // 恢复多选数组
            dictTypeDialogVisible: false,
            dictTypeDialogTitle: '新增字典类型',
            dictTypeForm: {
                id: null,
                dictName: '',
                dictType: ''
            },

            // 字典数据相关
            dictDataList: [],
            dictDataLoading: false,
            isAllDictDataSelected: false,
            dictDataDialogVisible: false,
            dictDataDialogTitle: '新增字典数据',
            dictDataForm: {
                id: null,
                dictTypeId: null,
                dictLabel: '',
                dictValue: '',
                sort: 0
            },
            search: '',
            // 添加分页相关数据
            pageSizeOptions: [10, 20, 50, 100],
            currentPage: 1,
            pageSize: 10,
            total: 0
        }
    },
    created() {
        this.loadDictTypeList()
    },
    methods: {
        // 字典类型相关方法
        loadDictTypeList() {
            this.dictTypeLoading = true
            dictApi.getDictTypeList({
                page: 1,
                limit: 100,
                dictName: this.search
            }, ({ data }) => {
                if (data.code === 0) {
                    this.dictTypeList = data.data.list
                    if (this.dictTypeList.length > 0) {
                        this.selectedDictType = this.dictTypeList[0]
                        this.loadDictDataList(this.dictTypeList[0].id)
                        this.$nextTick(() => {
                            this.$refs.dictTypeTable.setCurrentRow(this.dictTypeList[0])
                        })
                    }
                }
                this.dictTypeLoading = false
            })
        },
        handleDictTypeRowClick(row) {
            this.selectedDictType = row
            this.loadDictDataList(row.id)
            this.$refs.dictTypeTable.setCurrentRow(row)
        },
        handleDictTypeSelectionChange(val) {
            this.selectedDictTypes = val
        },
        tableRowClassName({ row }) {
            return row === this.selectedDictType ? 'current-row' : ''
        },
        showAddDictTypeDialog() {
            this.dictTypeDialogTitle = '新增字典类型'
            this.dictTypeForm = {
                id: null,
                dictName: '',
                dictType: ''
            }
            this.dictTypeDialogVisible = true
        },
        editDictType(row) {
            this.dictTypeDialogTitle = '编辑字典类型'
            this.dictTypeForm = { ...row }
            this.dictTypeDialogVisible = true
        },
        saveDictType(formData) {
            const api = formData.id ? dictApi.updateDictType : dictApi.addDictType
            api(formData, ({ data }) => {
                if (data.code === 0) {
                    this.$message.success('保存成功')
                    this.dictTypeDialogVisible = false
                    this.loadDictTypeList()
                }
            })
        },
        batchDeleteDictType() {
            if (this.selectedDictTypes.length === 0) {
                this.$message.warning('请选择要删除的字典类型')
                return
            }

            this.$confirm('确定要删除选中的字典类型吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const ids = this.selectedDictTypes.map(item => item.id)
                dictApi.deleteDictType(ids, ({ data }) => {
                    if (data.code === 0) {
                        this.$message.success('删除成功')
                        this.loadDictTypeList()
                    }
                })
            })
        },

        // 字典数据相关方法
        loadDictDataList(dictTypeId) {
            if (!dictTypeId) return
            this.dictDataLoading = true
            dictApi.getDictDataList({
                dictTypeId,
                page: this.currentPage,
                limit: this.pageSize,
                dictLabel: this.search,
                dictValue: ''
            }, ({ data }) => {
                if (data.code === 0) {
                    this.dictDataList = data.data.list.map(item => ({
                        ...item,
                        selected: false
                    }))
                    this.total = data.data.total
                } else {
                    this.$message.error(data.msg || '获取字典数据失败')
                }
                this.dictDataLoading = false
            })
        },
        selectAllDictData() {
            this.isAllDictDataSelected = !this.isAllDictDataSelected
            this.dictDataList.forEach(row => {
                row.selected = this.isAllDictDataSelected
            })
        },
        showAddDictDataDialog() {
            if (!this.selectedDictType) {
                this.$message.warning('请先选择字典类型')
                return
            }
            this.dictDataDialogTitle = '新增字典数据'
            this.dictDataForm = {
                id: null,
                dictTypeId: this.selectedDictType.id,
                dictLabel: '',
                dictValue: '',
                sort: 0
            }
            this.dictDataDialogVisible = true
        },
        editDictData(row) {
            this.dictDataDialogTitle = '编辑字典数据'
            this.dictDataForm = { ...row }
            this.dictDataDialogVisible = true
        },
        saveDictData(formData) {
            const api = formData.id ? dictApi.updateDictData : dictApi.addDictData
            api(formData, ({ data }) => {
                if (data.code === 0) {
                    this.$message.success('保存成功')
                    this.dictDataDialogVisible = false
                    this.loadDictDataList(formData.dictTypeId)
                }
            })
        },
        deleteDictData(row) {
            this.$confirm('确定要删除该字典数据吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                dictApi.deleteDictData([row.id], ({ data }) => {
                    if (data.code === 0) {
                        this.$message.success('删除成功')
                        this.loadDictDataList(row.dictTypeId)
                    }
                })
            })
        },
        batchDeleteDictData() {
            const selectedRows = this.dictDataList.filter(row => row.selected)
            if (selectedRows.length === 0) {
                this.$message.warning('请选择要删除的字典数据')
                return
            }

            this.$confirm(`确定要删除选中的${selectedRows.length}个字典数据吗?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const ids = selectedRows.map(item => item.id)
                dictApi.deleteDictData(ids, ({ data }) => {
                    if (data.code === 0) {
                        this.$message.success('删除成功')
                        this.loadDictDataList(this.selectedDictType.id)
                    }
                })
            })
        },
        handleSearch() {
            if (!this.selectedDictType) {
                this.$message.warning('请先选择字典类型')
                return
            }
            this.currentPage = 1
            this.loadDictDataList(this.selectedDictType.id)
        },
        // 添加分页相关方法
        handlePageSizeChange(val) {
            this.pageSize = val;
            this.currentPage = 1;
            this.loadDictDataList(this.selectedDictType?.id);
        },
        goFirst() {
            this.currentPage = 1;
            this.loadDictDataList(this.selectedDictType?.id);
        },
        goPrev() {
            if (this.currentPage > 1) {
                this.currentPage--;
                this.loadDictDataList(this.selectedDictType?.id);
            }
        },
        goNext() {
            if (this.currentPage < this.pageCount) {
                this.currentPage++;
                this.loadDictDataList(this.selectedDictType?.id);
            }
        },
        goToPage(page) {
            this.currentPage = page;
            this.loadDictDataList(this.selectedDictType?.id);
        }
    },
    computed: {
        pageCount() {
            return Math.ceil(this.total / this.pageSize);
        },
        visiblePages() {
            const pages = [];
            const maxVisible = 3;
            let start = Math.max(1, this.currentPage - 1);
            let end = Math.min(this.pageCount, start + maxVisible - 1);

            if (end - start + 1 < maxVisible) {
                start = Math.max(1, end - maxVisible + 1);
            }

            for (let i = start; i <= end; i++) {
                pages.push(i);
            }
            return pages;
        }
    }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.welcome {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: linear-gradient(to bottom, mix(white, $--color-primary, 80%), #fcfaf3);
    overflow: hidden;
}

.operation-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
}

.page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

.action-group {
    display: flex;
    align-items: center;
    gap: 16px;
}

.search-group {
    display: flex;
    gap: 10px;
}

.btn-search {
    background: $--color-primary;
    border: none;
    color: white;

    &:hover {
        background: lighten($--color-primary, 5%);
    }
}

.main-wrapper {
    flex: 1;
    margin: 0 22px 22px;
    border-radius: 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    background: rgba(253, 250, 241, 0.8);
    display: flex;
    overflow: hidden;
    min-height: 0;
}

.content-panel {
    flex: 1;
    display: flex;
    gap: 16px;
    padding: 16px;
    overflow: hidden;
}

.dict-type-panel {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 300px;
    background: white;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

    .dict-type-header {
        display: flex;
        gap: 10px;

        .el-button {
            flex: 1;
        }
    }

    .dict-type-table {
        flex: 1;
        overflow-y: auto;

        ::v-deep .el-table__body tr.current-row>td {
            background-color: mix($--color-primary, white, 85%) !important;
        }

        ::v-deep .el-table__row:hover>td {
            background-color: #f5f7fa !important;
        }
    }
}

.content-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.dict-data-card {
    flex: 1;
    background: white;
    border-radius: 8px;
    border: none;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
    overflow: hidden;

    ::v-deep .el-card__body {
        padding: 0;
        display: flex;
        flex-direction: column;
        flex: 1;
        overflow: hidden;
    }
}

.data-table {
    flex: 1;
    overflow-y: auto;

    ::v-deep .table-header th {
        background-color: #f7f9fc !important;
    }
}

.table-footer {
    padding: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
}

.batch-actions {
    display: flex;
    gap: 10px;
}

.el-button--primary {
    background: $--color-primary;
    border-color: $--color-primary;
    color: white;

    &:hover {
        background: lighten($--color-primary, 5%);
        border-color: lighten($--color-primary, 5%);
    }
}

.el-button--success {
    background: #5bc98c;
    border-color: #5bc98c;
    color: white;
}

.el-button--danger {
    background: #fd5b63;
    border-color: #fd5b63;
    color: white;
}

.edit-btn,
.delete-btn {
    color: $--color-primary !important;

    &:hover {
        color: darken($--color-primary, 10%) !important;
    }
}

.custom-pagination {
    display: flex;
    align-items: center;
    gap: 5px;

    .page-size-select {
        width: 100px;
    }

    .pagination-btn {
        min-width: 32px;
        height: 32px;
        padding: 0 6px;
        border-radius: 4px;
        border: 1px solid #e4e7ed;
        background: #fff;
        color: #606266;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover:not(:disabled) {
            color: $--color-primary;
            border-color: mix($--color-primary, white, 80%);
        }

        &.active {
            background: $--color-primary !important;
            color: #ffffff !important;
            border-color: $--color-primary !important;
        }

        &:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
    }
}

.total-text {
    margin-left: 10px;
    color: #606266;
    font-size: 14px;
}

::v-deep .el-table {
    th {
        background-color: #f7f9fc !important;
    }

    .el-checkbox__input.is-checked .el-checkbox__inner {
        background-color: $--color-primary;
        border-color: $--color-primary;
    }
    .el-checkbox__inner:hover {
        border-color: $--color-primary;
    }
}
</style>