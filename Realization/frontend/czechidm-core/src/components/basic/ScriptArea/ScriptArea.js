import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import FormComponentLabel from '../AbstractFormComponent/FormComponentLabel';
import Tooltip from '../Tooltip/Tooltip';
import Button from '../Button/Button';
import Modal from '../Modal/Modal';

/**
 * Script Area.
 *
 * @author Vít Švanda
 */
class ScriptArea extends AbstractFormComponent {

  componentDidMount() {
    super.componentDidMount();
    this.initCompleters();
  }

  initCompleters() {
    const {completers} = this.props;

    const component = this._getAceComponent();
    if (component) {
      const editor = component.editor;
      // For prevent add a redundant completers.
      const originalCompleters = editor.completers
        .filter(completer => completer)
        .filter(completer => !completer.hasOwnProperty('isCustomCompleter') || !completer.isCustomCompleter());
      editor.completers = originalCompleters;
      // Convert custom completers for Ace editor.
      const customCompleters = this._getCustomCompleter(completers);
      if (customCompleters) {
        editor.completers = [...originalCompleters, customCompleters];
      }

      // Set size for suggestion dialog.
      if (!editor.completer) {
        // make sure completer is initialized
        editor.execCommand('startAutocomplete');
        editor.completer.detach();
      }
      if (editor.completer && editor.completer.popup && editor.completer.popup.container) {
        editor.completer.popup.container.style.width = '40%';
      }
    }
  }

  _getAceComponent() {
    const {showModalEditor} = this.state;
    return this.refs[showModalEditor ? 'inputModal' : 'input'];
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  /**
   * Focus input field
   */
  focus() {
    const { showModalEditor } = this.state;
    if (showModalEditor) {
      this.refs.inputModal.editor.focus();
    } else {
      this.refs.input.editor.focus();
    }
  }

  onChange(newValue) {
    if (this.props.onChange) {
      this.props.onChange(newValue);
    } else {
      this.setState({
        value: newValue
      }, () => {
        this.validate();
      });
    }
  }

  _closeModalEditor() {
    this.setState({showModalEditor: false});
  }

  _showModalEditor() {
    this.setState({showModalEditor: true}, () => {
      setTimeout(() => {
        this.initCompleters();
      }, 10);
    });
  }

  _getCustomCompleter(completers) {
    if (!completers) {
      return null;
    }
    return ({
      getCompletions(editor, session, pos, prefix, callback) {

        callback(null, completers.map((completer) => {
          return {
            caption: completer.name, // This value is show in the whispering dialog.
            name: completer.name,
            value: completer.value ? completer.value : completer.name, // This value will be pushed to the editor.
            score: completer.score ? completer.score : 1000, // Order in the whispering dialog.
            meta: completer.returnType, // Return type
            description: completer.description // Help in the whispering dialog.
          };
        }));
      },
      getDocTooltip(item) {
        return item.description;
      },
      isCustomCompleter() {
        return true;
      }
    });
  }

  _getAceEditor(AceEditor, mode, className, height, modal = false) {

    return (
      <AceEditor
        ref={modal ? 'inputModal' : 'input'}
        mode={mode}
        width={null}
        height={modal ? '40em' : height}
        className={className}
        title={this.getValidationResult() != null ? this.getValidationResult().message : ''}
        readOnly={this.state.readOnly}
        enableBasicAutocompletion
        enableLiveAutocompletion
        wrapEnabled={false}
        theme="tomorrow"
        onChange={this.onChange}
        value={this.state.value || ''}
        tabSize={4}
        fontSize={14}
        spellcheck
        showGutter
        editorProps={{$blockScrolling: 'Infinity'}}
        setOptions={{
          enableBasicAutocompletion: true,
          enableLiveAutocompletion: true
        }}
      />);
  }

  _getMaximalizationButton(showMaximalizationBtn) {
    return (
      <Button
        type="button"
        buttonSize="xs"
        level="success"
        rendered={showMaximalizationBtn}
        onClick={ this._showModalEditor.bind(this) }
        icon="fullscreen"/>
    );
  }

  getOptionsButton() {
    const { showMaximalizationBtn } = this.props;
    return (
      <div className="pull-right script-area-btn-max">
        { this._getMaximalizationButton(showMaximalizationBtn) }
        { this.renderHelpIcon() }
      </div>
    );
  }

  _getComponent(feedback) {
    const { labelSpan, label, placeholder, componentSpan, required, mode, height } = this.props;
    const { showModalEditor, disabled, readOnly } = this.state;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !this.state.value) {
      showAsterix = true;
    }

    // Workaround - Import for AceEditor must be here. When was on start, then not working tests (error is in AceEditor);
    const AceEditor = require('react-ace').default;
    require('brace/mode/groovy');
    require('brace/mode/json');
    require('brace/mode/sqlserver');
    require('brace/mode/ruby');
    require('brace/theme/github');
    require('brace/theme/tomorrow');
    require('brace/ext/language_tools');
    const AceEditorInstance = this._getAceEditor(AceEditor, mode, className, height, showModalEditor);
    const _label = [];
    if (label) {
      _label.push(label);
    } else if (placeholder) {
      _label.push(placeholder);
    }
    if (_label.length > 0 && required) {
      _label.push(' *');
    }
    //
    return (
      <div className={
        classNames(
          'basic-form-component',
          { 'has-feedback': feedback },
          { disabled: disabled || readOnly }
        )
      }>
        <FormComponentLabel
          className={ labelClassName }
          label={ _label }/>
        <div className={componentSpan}>
          <Tooltip ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
            <span>
              { this.getOptionsButton() }
              {/* Editor cannot be hidden here if modal is show, because Ace editor will be null after closing the modal dialog.*/}
              {AceEditorInstance}
              {
                feedback
                ||
                !showAsterix
                ||
                <span className="form-control-feedback" style={{color: 'red', zIndex: 0}}>*</span>
              }
              <Modal
                show={showModalEditor}
                dialogClassName="modal-large"
                onHide={this._closeModalEditor.bind(this)}>
                <Modal.Header text={label}/>
                <Modal.Body style={{overflow: 'scroll'}}>
                  {AceEditorInstance}
                </Modal.Body>
                <Modal.Footer>
                  <Button level="link" onClick={this._closeModalEditor.bind(this)}>{this.i18n('button.close')}</Button>
                </Modal.Footer>
              </Modal>
            </span>
          </Tooltip>
          { this.renderHelpBlock() }
        </div>
      </div>
    );
  }

  getBody(feedback) {
    return this._getComponent(feedback);
  }
}

ScriptArea.propTypes = {
  ...AbstractFormComponent.propTypes,
  helpBlock: PropTypes.string,
  mode: PropTypes.string,
  height: PropTypes.string
};

ScriptArea.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  mode: 'groovy',
  height: '10em',
  showMaximalizationBtn: true
};

export default ScriptArea;
