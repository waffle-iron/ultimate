// Boogie file generated by EOG prover
procedure product() {
	var _global_next : int;
	var _global_workload : int;
	var _local__threadpooling_working_0 : int;
	var _local__threadpooling_working_1 : int;
	var _local__threadpooling_i_0 : int;
	var _local__threadpooling_i_1 : int;
	var _local__threadpooling_end_0 : int;
	var _local__threadpooling_end_1 : int;
	var _local__threadpooling_begin_0 : int;
	var _local__threadpooling_begin_1 : int;
	assume 0 < _global_workload && 0 < _global_next;
	assume -1 == _local__threadpooling_working_0;
	assume -1 == _local__threadpooling_working_1;

	$l1$l1: if (*) {
		goto $l2$l1;
	} else {
		goto $l1$l2;
	}
	$l1$l2: if (*) {
		goto $l2$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l1$l3;
	}
	$l1$l3: if (*) {
		goto $l2$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l1$l4;
	}
	$l1$l4: if (*) {
		goto $l2$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l1$l5;
		} else {
			goto $l1$l1;
		}
	}
	$l1$l5: if (*) {
		goto $l2$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l1$l6;
	}
	$l1$l6: if (*) {
		goto $l2$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l1$l7;
	}
	$l1$l7: if (*) {
		goto $l2$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l1$l4;
	}
	$l2$l7: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l2$l4;
	}
	$l3$l7: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l3$l4;
	}
	$l3$l4: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l3$l5;
		} else {
			goto $l3$l1;
		}
	}
	$l3$l1: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l1;
	} else {
		goto $l3$l2;
	}
	$l3$l2: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l3$l3;
	}
	$l3$l3: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l3$l4;
	}
	$l4$l3: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l3;
		} else {
			goto $l1$l3;
		}
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l4$l4;
	}
	$l5$l3: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l5$l4;
	}
	$l5$l4: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l5$l5;
		} else {
			goto $l5$l1;
		}
	}
	$l5$l1: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l1;
	} else {
		goto $l5$l2;
	}
	$l5$l2: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l5$l3;
	}
	$l6$l2: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l6$l3;
	}
	$l7$l2: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l7$l3;
	}
	$l7$l3: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l7$l4;
	}
	$l7$l4: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l7$l5;
		} else {
			goto $l7$l1;
		}
	}
	$l7$l1: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l1;
	} else {
		goto $l7$l2;
	}
	$l7$l5: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l7$l6;
	}
	$l7$l6: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l7;
	}
	$l7$l7: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_i_0 + 1;
		goto $l4$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l7$l4;
	}
	$l4$l6: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l6;
		} else {
			goto $l1$l6;
		}
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l4$l7;
	}
	$l5$l6: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l5$l7;
	}
	$l5$l7: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l5$l4;
	}
	$l6$l7: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l7;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l6$l4;
	}
	$l6$l6: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l6$l7;
	}
	$l4$l5: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l5;
		} else {
			goto $l1$l5;
		}
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l4$l6;
	}
	$l6$l1: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l1;
	} else {
		goto $l6$l2;
	}
	$l5$l5: if (*) {
		_local__threadpooling_working_0 := _local__threadpooling_i_0;
		goto $l6$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l5$l6;
	}
	$l6$l5: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l6$l6;
	}
	$l6$l4: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l6$l5;
		} else {
			goto $l6$l1;
		}
	}
	$l6$l3: if (*) {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l7$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l6$l4;
	}
	$l4$l2: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l2;
		} else {
			goto $l1$l2;
		}
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l4$l3;
	}
	$l4$l1: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l1;
		} else {
			goto $l1$l1;
		}
	} else {
		goto $l4$l2;
	}
	$l3$l5: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l3$l6;
	}
	$l3$l6: if (*) {
		_local__threadpooling_i_0 := _local__threadpooling_begin_0;
		goto $l4$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l3$l7;
	}
	$l4$l4: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l4;
		} else {
			goto $l1$l4;
		}
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l4$l5;
		} else {
			goto $l4$l1;
		}
	}
	$l4$l7: if (*) {
		if (_local__threadpooling_i_0 < _local__threadpooling_end_0) {
			goto $l5$l7;
		} else {
			goto $l1$l7;
		}
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_i_1 + 1;
		goto $l4$l4;
	}
	$l2$l6: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l6;
	} else {
		assert (true || !((_local__threadpooling_working_0 == _local__threadpooling_working_0))) && (false || !((_local__threadpooling_working_0 == _local__threadpooling_working_1))) && (false || !((_local__threadpooling_working_1 == _local__threadpooling_working_0)));
		goto $l2$l7;
	}
	$l2$l5: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l5;
	} else {
		_local__threadpooling_working_1 := _local__threadpooling_i_1;
		goto $l2$l6;
	}
	$l2$l4: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l4;
	} else {
		if (_local__threadpooling_i_1 < _local__threadpooling_end_1) {
			goto $l2$l5;
		} else {
			goto $l2$l1;
		}
	}
	$l2$l3: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l3;
	} else {
		_local__threadpooling_i_1 := _local__threadpooling_begin_1;
		goto $l2$l4;
	}
	$l2$l2: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l2;
	} else {
		_local__threadpooling_begin_1 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_1 := _global_next;
		goto $l2$l3;
	}
	$l2$l1: if (*) {
		_local__threadpooling_begin_0 := _global_next;
		_global_next := _global_next + _global_workload;
		_local__threadpooling_end_0 := _global_next;
		goto $l3$l1;
	} else {
		goto $l2$l2;
	}
	endend:
}