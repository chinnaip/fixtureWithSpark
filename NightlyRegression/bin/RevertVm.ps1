#
# RevertVm.ps1 - Powershell script to revert the virtual test machines to a specific snapshot
#                Usage: ReverVm.ps1 Server Username Password
#

# Check arguments
If ($args.Length -lt 5)
{
	Write-Host "Not enough arguments passed.";
	Write-Host "Usage: ReverVm.ps1 Server Username Password";
	exit -1;
}
 
$server=$args[0]
$user=$args[1] 
$password=$args[2]
$vm=$args[3] 
$snapshot=$args[4] 

# Load the VMware core functions
add-pssnapin VMware.VimAutomation.Core

# Connect to the ESX Server
Connect-VIServer -Server "$server" -User "$user" -Password "$password"

# Reset the VM's
Set-VM -VM "$vm" -Snapshot "$snapshot" -Confirm:$false
Start-VM -VM "$vm" -Confirm:$false 
