/* eslint-disable prefer-template */
window.HystrixThreadPoolContainerTemplate = (props) => {
    const { name, index, errorPercentage } = props;
    let displayName = name;
    let toolTip = '';
    if (displayName.length > 32) {
        displayName = `${displayName.substring(0, 4)}...${displayName.substring(
            displayName.length - 20,
            displayName.length
        )}`;
        toolTip = `title="${name}"`;
    }
    return `
        <div class="monitor" id="THREAD_POOL_${name + '_' + index}" style="position:relative;">
        
        <div id="chart_THREAD_POOL_${
            name + '_' + index
        }" class="chart" style="position:absolute;top:0px;left:0; float:left; width:100%; height:100%;"></div>
        <div style="position:absolute;top:0;width:100%;height:15px;opacity:0.8; background:white;"><p class="name" ${ toolTip }>${ displayName }</p></div>
        <div style="position:absolute;top:15px;; opacity:0.8; background:white; width:100%; height:95%;">
            <div class="monitor_data"></div>
        </div>
        

        <script>
            var y = 200;
            /* escape with two backslashes */
            var vis = d3.select("#chart_THREAD_POOL_${
                name.replace(/([ !"#$%&'()*+,./:;<=>?@[]^\`{|}~])/g, '\\\\$1') + '_' + index
            }").append("svg:svg").attr("width", "100%").attr("height", "100%");
            /* add a circle -- we don't use the data point, we set it manually, so just passing in [1] */
            var circle = vis.selectAll("circle").data([1]).enter().append("svg:circle");
            /* setup the initial styling and sizing of the circle */
            circle.style("fill", "green").attr("cx", "30%").attr("cy", "20%").attr("r", 5);
        </script>
        
    </div>
    `;
};
